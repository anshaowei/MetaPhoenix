# Using databases created by MSLibrary

MetaPhoenix was previously named MSLibrary. Databases created with MSLibrary, including the `mslibrary` database used for the paper, can be used as they are without migration:

```bash
export MONGODB_URI=mongodb://localhost:27017/mslibrary
```

* Documents written by MSLibrary store their Java type as `_class: net.csibio.mslibrary.*`. MetaPhoenix maps these to the current `net.csibio.metaphoenix.*` classes when reading (see `LegacyTypeInformationMapper`). Documents written from now on use the new class names, and old and new documents can live in the same collection.
* Collection names (`spectrum-<libraryId>`, `spectrum-<libraryId>-<DecoyStrategy>`, `library`, ...) are unchanged.
* Statistics are still stored under the key `MSLibraryCompoundNum` so that existing `stat` documents stay readable.
* To analyse an old database without changing it, also start with `--spring.data.mongodb.auto-index-creation=false`. Otherwise Spring creates any missing indexes at startup. Commands such as `filter` and `decoy` write to the database.
* If Redis caching is enabled, flush the cache once after upgrading. Cached entries store class names under the old package and cannot be read by the new code.
