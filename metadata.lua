-- metadata.lua — Solar2D plugin metadata for plugin.singular
local metadata = {
    plugin = {
        format            = 'staticLibrary',
        androidEntryPoint = 'plugin.singular.LuaLoader',
        supportedPlatforms = {
            android       = { marketplaceId = "" },
            iphone        = { marketplaceId = "" },
            ["mac-sim"]   = false,
            ["win32-sim"] = false,
        },
    },
}
return metadata
