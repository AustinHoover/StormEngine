
/**
 * @description The compiler object
 */
let COMPILER = {

    //
    //
    //    VIRTUAL FILE SYSTEM
    //
    //

    /**
     * The map of all source files to their content and compiled value
     */
    fileMap: { },

    /**
     * The list of all source files to compile
     */
    sourceFiles: [ ],

    /**
     * The top level directory, "/"
     */
    topLevelDirectory: {
        //as required by our framework
        Scripts: {
            compiler: {
                "host_access.js": {
                    content: "",
                    version: 0,
                },
                version: 0,
                isDir: true,
            },
            version: 0,
            isDir: true,
        },
        //as required by language service
        node_modules: {
            "@types": {
                "lib.d.ts": {
                    content: "",
                    version: 0,
                    isDir: false,
                },
                version: 0,
                isDir: true,
            },
            version: 0,
            isDir: true,
        },
        version: 0,
        isDir: true,
    },

    /**
     * The current directory, "/"
     */
    currentDirectory : { },


    /**
     * Preloads a file from the host system's cache
     * @param {*} fileName The name of the file
     * @param {*} content The content of the file
     */
    preloadFile: (fileName, content) => {
        COMPILER.fileMap[fileName] = COMPILER.createFile(fileName, content)
        COMPILER.fileMap[fileName].moduleContent = COMPILER.getModuleContent(content)
    },

    /**
     * Gets the module content from generic file content
     * @param {*} content The file content
     * @returns The module content
     */
    getModuleContent: (content) => {
        return "let exports = { }\n" +
        content + "\n" +
        "return exports"
    },

    /**
     * Registers a file with the compiler
     * @param {string} fileName The file's name
     * @param {string} content The content of the file
     * @returns {string[]} The list of all files that still need to be registered by the host
     */
    registerFile: (fileName, content) => {

        //the list of files that are imported by this file
        let dependentFiles = []
    
        loggerScripts.INFO('REGISTER FILE ' + fileName)
        if(!COMPILER.fileMap[fileName]){
            //create the virtual file
            COMPILER.fileMap[fileName] = COMPILER.createFile(fileName,content)
            //register the file itself
            COMPILER.fileMap[fileName].tsSourceFile = ts.createSourceFile(
                fileName,
                content,
                ts.ScriptTarget.Latest,
            )
            COMPILER.sourceFiles.push(fileName)
            /**
             * The preprocessed info about the file
             * {
             *   referencedFiles: ?,
             *   typeReferenceDirectives: ?,
             *   libReferenceDirectives: ?,
             *   importedFiles: Array<{
             *     fileName: string, //the path (without file ending) of the file that is imported by this file
             *     pos: ?,
             *     end: ?,
             *   }>,
             *   isLibFile: boolean,
             *   ambientExternalModules: ?,
             * }
             */
            const fileInfo = ts.preProcessFile(content)
            loggerScripts.INFO('==========================')
            loggerScripts.INFO(fileName)
            loggerScripts.INFO('Registered file depends on:')
            fileInfo.importedFiles.forEach(module => {
                let extension = ".ts"
                /**
                 * {
                 *   resolvedModule: ?,
                 *   failedLookupLocations: Array<string>,
                 *   affectingLocations: ?,
                 *   resolutionDiagnostics: ?,
                 *   alternateResult: ?,
                 * }
                 */
                const resolvedImport = ts.resolveModuleName(module.fileName,fileName,COMPILER.compilerOptions,COMPILER.customCompilerHost)
                if(resolvedImport?.resolvedModule){
                    /**
                     * undefined
                     * OR
                     * {
                     *   resolvedFileName: ?,
                     *   originalPath: ?,
                     *   extension: string, (ie ".js", ".ts", etc)
                     *   isExternalLibraryImport: boolean,
                     *   packageId: ?,
                     *   resolvedUsingTsExtension: boolean,
                     * }
                     */
                    const module = resolvedImport.resolvedModule
                    extension = module.extension
                }
                //am assuming we're always importing typescript for the time being
                const dependentFile = module.fileName + extension
                const normalizedDependentFilePath = FILE_RESOLUTION_getFilePath(dependentFile,false)
                if(!COMPILER.fileMap[normalizedDependentFilePath]){
                    dependentFiles.push(normalizedDependentFilePath)
                    loggerScripts.INFO(" - " + normalizedDependentFilePath)
                }
            })
    
            //If the compiler has already run once, run the language service against only this file
            if(!!COMPILER.compilerHasRun){
                COMPILER.emitFile(fileName)
            }
        }
        return dependentFiles;
    },

    /**
     * Creates a file object for a given path
     * @param string} fileName The name of the file
     * @param {string} content The content of the file
     * @returns The file object
     */
    createFile: (fileName, content) => {
        //get the file path array
        const filePathArray = COMPILER.getPath(fileName)
        let mutableArray = filePathArray
    
        //the current folder as we recursively create folders to populate this file
        let currentFolder = COMPILER.topLevelDirectory
    
        //recursively create directories until our file is written
        while(mutableArray.length > 1){
            let nextDirName = mutableArray.shift()
            if(!currentFolder?.[nextDirName]){
                //create directory
                currentFolder[nextDirName] = {
                    isDir: true,
                    "..": currentFolder,
                }
            }
            currentFolder = currentFolder?.[nextDirName]
        }
    
        //create the actual file
        currentFolder[mutableArray[0]] = {
            isDir: false,
            dir: currentFolder,
            content: content,
            version: 0,
        }
    
        //return the file
        return currentFolder[mutableArray[0]]
    },

    /**
     * Gets the path for the file
     * @param {string} fullyQualifiedFilePath The fully qualified file path
     * @returns {string[]} The array of directories ending with the name of the file
     */
    getPath: (fullyQualifiedFilePath) => {
        let modifiedFileName = fullyQualifiedFilePath
        //remove leading "/"
        if(modifiedFileName.startsWith("/")){
            modifiedFileName = modifiedFileName.substring(1)
        }
        //split
        return modifiedFileName.split("/")
    },

    /**
     * Gets the path for the file
     * @param {stringp[]} filePathArray The fully qualified file path
     * @returns The array of directories ending with the name of the file
     */
    getFileByPath: (filePathArray) => {
        let currentFolder = COMPILER.topLevelDirectory
        let mutableArray = filePathArray
    
        //illegal state
        if(mutableArray?.length < 1){
            throw new Error("Trying to get a file with a path array of length 0!")
        }
    
        while(mutableArray?.length > 1){
            let nextDirName = mutableArray.shift()
            currentFolder = currentFolder?.[nextDirName]
            if(!currentFolder){
                let errorMessage = "Trying to get file in directory that doesn't exist! \n" +
                nextDirName
                throw new Error(errorMessage)
            }
        }
        return currentFolder[mutableArray?.[0]]
    },

    /**
     * Checks if a file exists
     * @param {string[]} filePathArray The file path array
     * @returns true if it exists, false otherwise
     */
    fileExists: (filePathArray) => {
        let currentFolder = COMPILER.topLevelDirectory
        let mutableArray = filePathArray
    
        //illegal state
        if(mutableArray?.length < 1){
            throw new Error("Trying to get a file with a path array of length 0!")
        }
    
        while(mutableArray.length > 1){
            let nextDirName = mutableArray.shift()
            currentFolder = currentFolder?.[nextDirName]
            if(!currentFolder){
                return false
            }
        }
        return !!currentFolder?.[mutableArray[0]]
    },

    /**
     * The callback invoked when the compiler host tries to read a file
     * @param {string} fileName The name of the file
     * @param {*} languageVersion The language version
     * @returns The file if it exists, null otherwise
     */
    getSourceFile: (fileName, languageVersion) => {
        if(!!COMPILER.fileMap[fileName]){
            return COMPILER.fileMap[fileName].tsSourceFile
        } else {
            return null
        }
    },


    //
    //
    //   COMPILATION
    //
    //

    /**
     * The compiler options
     */
    compilerOptions: { },

    /**
     * Tracks whether the compiler has run or not
     */
    compilerHasRun: false,

    /**
     * The typescript compiler host definition
     */
    customCompilerHost: null,

    /**
     * The typescript program
     */
    program: null,

    /**
     * Emits a file
     * @param {string} fileName The name of the file
     * @returns {void}
     */
    emitFile: (fileName) => {
        loggerScripts.DEBUG('Compiler evaluating source path ' + fileName)
        /**
         * {
         *   outputFiles: [ ],
         *   emitSkipped: boolean,
         *   diagnostics: { },
         * }
         */
        const output = COMPILER.program.getEmitOutput(fileName)
        if (!output.emitSkipped) {
            output.outputFiles.forEach(outputFile => {
                loggerScripts.DEBUG(`[ts] Emitting ${outputFile}`);
                COMPILER.customCompilerHost.writeFile(outputFile.name, outputFile.text)
            })
        } else {
            loggerScripts.DEBUG(`[ts] Emitting ${fileName} failed`);
            COMPILER.logEmitError(fileName);
        }
    },

    /**
     * Logs errors raised during emission of files
     * @param {string} fileName The name of the file to log errors about
     * @returns {void}
     */
    logEmitError: (fileName) => {
        loggerScripts.DEBUG('[ts] logErrors ' + fileName)
        let allDiagnostics = services
            .getCompilerOptionsDiagnostics()
            .concat(services.getSyntacticDiagnostics(fileName))
            .concat(services.getSemanticDiagnostics(fileName));
    
        allDiagnostics.forEach(diagnostic => {
            let message = ts.flattenDiagnosticMessageText(diagnostic.messageText, "\n");
            if (diagnostic.file) {
                let { line, character } = diagnostic.file.getLineAndCharacterOfPosition(
                    diagnostic.start
                );
                loggerScripts.DEBUG(`[ts]  Error ${diagnostic.file.fileName} (${line + 1},${character +1}): ${message}`);
            } else {
                loggerScripts.DEBUG(`[ts]  Error: ${message}`);
            }
        });
    },

    /**
     * Instructs Typescript to emit the final compiled value
     */
    run: () => {
        loggerScripts.INFO('COMPILE ALL REGISTERED FILES')
    
        if(!COMPILER.program){
            COMPILER.program = ts.createLanguageService(COMPILER.customCompilerHost, ts.createDocumentRegistry());
        }
    
        //Emit all currently known files
        COMPILER.sourceFiles.forEach(sourcePath => {
            COMPILER.emitFile(sourcePath)
        })
    
        //flag that the compiler has run (ie only incrementally compile when new files are added, now)
        COMPILER.compilerHasRun = true
    },

    /**
     * Loads a file
     * @param {*} fileName The name of the file to load (preferably already has .ts at the end)
     */
    runFile: (fileName) => {
        let normalizedFilePath = FILE_RESOLUTION_getFilePath(fileName)
        if(!!COMPILER.fileMap[normalizedFilePath]){
            loggerScripts.INFO('RUN FILE ' + normalizedFilePath)
            eval(COMPILER.fileMap[normalizedFilePath].content)
        } else {
            const message = 'FAILED TO RESOLVE FILE ' + normalizedFilePath
            loggerScripts.WARNING(message)
            throw new Error(message)
        }
    },

    /**
     * Loads a file
     * @param {*} fileName The name of the file to load (preferably already has .ts at the end)
     */
    printSource: (fileName) => {
        let normalizedFilePath = FILE_RESOLUTION_getFilePath(fileName)
        if(!!COMPILER.fileMap[normalizedFilePath]){
            loggerScripts.INFO('FILE CONTENT ' + normalizedFilePath)
        } else {
            const message = 'FAILED TO RESOLVE FILE ' + normalizedFilePath
            loggerScripts.WARNING(message)
            loggerScripts.WARNING('file map content:')
            loggerScripts.WARNING(OBject.keys(COMPILER.fileMap) + "")
            throw new Error(message)
        }
    },
}


/**
 * Constructs the compiler host
 * https://www.typescriptlang.org/tsconfig/#compilerOptions
 * 
 * Examples:
 * https://github.com/microsoft/TypeScript/wiki/Using-the-Compiler-API
 * 
 */
COMPILER.customCompilerHost = {
    getSourceFile: COMPILER.getSourceFile,
    writeFile: (fileName, data) => {
        loggerScripts.INFO("EMIT FILE " + fileName)
        //wrap in require logic
        let finalData = COMPILER.getModuleContent(data)

        //create file
        COMPILER.createFile(fileName,finalData)
        
        //register in file map
        COMPILER.fileMap[fileName] = {
            content: data, //to be eval'd from top level
            moduleContent: finalData, //to be eval'd from require()
        }
    },
    getDefaultLibFileName: ts.getDefaultLibFileName,
    useCaseSensitiveFileNames: () => false,
    getCanonicalFileName: filename => filename,
    getCurrentDirectory: () => "/",
    getNewLine: () => "\n",
    getDirectories: (path) => {
        loggerScripts.DEBUG('[ts] getDirectories ' + path)
        const dirs = Object.keys(COMPILER.getFileByPath(COMPILER.getPath(path)))
        loggerScripts.DEBUG('[ts] dirs: ' + dirs)
        return dirs
    },
    directoryExists: (path) => {
        let exists = COMPILER.fileExists(COMPILER.getPath(path))
        if(exists){
            exists = COMPILER.getFileByPath(COMPILER.getPath(path))?.isDir
        }
        loggerScripts.DEBUG('[ts] directoryExists ' + path + " - " + exists)
        return false
    },
    fileExists: (path) => {
        const exists = COMPILER.fileExists(COMPILER.getPath(path))
        loggerScripts.DEBUG('[ts] fileExists ' + path + " - " + exists)
        return exists
    },
    readFile: (path) => {
        loggerScripts.DEBUG('[ts] readFile ' + path)
        const file = COMPILER.getFileByPath(COMPILER.getPath(path))
        loggerScripts.DEBUG('[ts] readFile (content): ' + file.content)
        return file.content
    },
    getScriptFileNames: () => {
        loggerScripts.DEBUG('[ts] getScriptFileNames')
        return COMPILER.sourceFiles
    },
    getScriptVersion: (fileName) => {
        loggerScripts.DEBUG('[ts] getScriptVersion: ' + fileName)
        const file = COMPILER.getFileByPath(COMPILER.getPath(fileName))
        return file?.version
    },
    //https://github.com/microsoft/TypeScript/wiki/Using-the-Language-Service-API#scriptsnapshot
    getScriptSnapshot: (fileName) => {
        loggerScripts.DEBUG('[ts] getScriptSnapshot: ' + fileName)
        const file = COMPILER.getFileByPath(COMPILER.getPath(fileName))
        if(file){
            return ts.ScriptSnapshot.fromString(file.content)
        } else {
            return undefined
        }
    },
    getCompilationSettings: () => COMPILER.compilerOptions,
}

//initialized CWD
COMPILER.currentDirectory = COMPILER.topLevelDirectory
