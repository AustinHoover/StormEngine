
/**
 * Normalizes a file path
 * @param {*} rawFilePath The raw file path 
 * @returns The normalized file path
 */
const FILE_RESOLUTION_getFilePath = (rawFilePath, isJavascript = true) => {
    let fileName = rawFilePath
    if(isJavascript && fileName.includes('.ts')){
        fileName = fileName.replace('.ts','.js')
    }
    if(fileName.startsWith('/Scripts')){
        // fileName = fileName.replace('/Scripts','')
    }
    if(fileName.startsWith('Scripts')){
        fileName = fileName.replace('Scripts','/Scripts')
    }
    if(isJavascript && !fileName.endsWith(".js")){
        fileName = fileName + ".js"
    }
    return fileName
}

