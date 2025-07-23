

/**
 * Caches loaded modules
 */
let REQUIRE_CACHE = { }

/**
 * Used if the module is directly executed instead of being require'd for some reason
 */
let exports = { }

/**
 * Imports a module
 * @param {*} path The path of the module
 * @param {*} cwd The current working directory
 */
const require = (path) => {
    //get the path
    loggerScripts.DEBUG('Require path ' + path)
    let normalizedFilePath = FILE_RESOLUTION_getFilePath(path)

    //actually require
    if(!!REQUIRE_CACHE[normalizedFilePath]){
        //return if has already been required
        return REQUIRE_CACHE[normalizedFilePath].exports
    } else if(!!COMPILER.fileMap[normalizedFilePath]?.content) {
        //require if it is already registered
        const code = COMPILER.fileMap[normalizedFilePath].moduleContent
        loggerScripts.DEBUG('Module code ' + JSON.stringify(code))
        //create dummy prior to fully evaluating code so that we don't recurse infinitely
        REQUIRE_CACHE[normalizedFilePath] = {
            exports: {},
            exportedValues: [],
        }
        //evaluate script for exports
        let exports = new Function(code)()
        //create module object
        const module = {
            exports: exports,
            exportedValues: Object.keys(exports),
        }
        REQUIRE_CACHE[normalizedFilePath] = module
        loggerScripts.INFO("[require] CREATE MODULE " + normalizedFilePath)
        return module.exports
    } else {
        //fail if it doesn't exist from host's view
        const errorMsg = "FAILED TO REQUIRE FILE " + normalizedFilePath
        loggerScripts.WARNING(errorMsg)
        loggerScripts.WARNING('Module value:')
        const cacheValue = REQUIRE_CACHE?.[normalizedFilePath]
        loggerScripts.WARNING(Object.keys(cacheValue ? cacheValue : {}) + '')
        loggerScripts.WARNING('Require cache contents:')
        loggerScripts.WARNING(Object.keys(REQUIRE_CACHE) + '')
        loggerScripts.WARNING('File cache contents:')
        loggerScripts.WARNING(Object.keys(COMPILER.fileMap) + '')
        throw new Error(errorMsg)
    }
}

//Add require to its own cache
REQUIRE_CACHE["/Scripts/compiler/require_polyfill.js"] = {
    exports: {
        'require': require,
        'exports': exports,
    },
    exportedValues: [
        'require',
        'exports',
    ],
}
