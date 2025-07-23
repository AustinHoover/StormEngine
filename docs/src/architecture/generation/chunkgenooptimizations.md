@page chunkgenoptimizations Chunk Generation Optimizations

Strategies to try:
 - Cache "is surface" "is sky" "is cave" at generator level, then looking x,z against cached values to find whether the newly requested chunk is surface or not