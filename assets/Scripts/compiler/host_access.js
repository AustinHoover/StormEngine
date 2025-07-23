
/**
 * The host context that contains the core engine functions
 */
let HOST_ACCESS = {
    classes: { }, //the classes available to the script engine
    singletons: { }, //the singletons available to the script engine
}

//fake require
REQUIRE_CACHE["/Scripts/compiler/host_access.js"] = {
    exports: {
        'HOST_ACCESS': HOST_ACCESS,
        'loggerScripts': loggerScripts,
    },
    exportedValues: [
        'HOST_ACCESS',
        'loggerScripts',
    ],
}

