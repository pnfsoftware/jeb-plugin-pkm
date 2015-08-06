# jeb2-plugin-pkm

JEB2 Plugin for parsing Android PKM files (ETC1 compressed images)

A PKM file consists of a 16 byte header followed by ETC1 compressed texture data. Details on the ETC1 file
format can be found here: http://www.khronos.org/registry/gles/extensions/OES/OES_compressed_ETC1_RGB8_texture.txt

This plugin relies on the etc1tool, for conversion purposes, that is shipped with the Android SDK tools.

Authored By:
Carlos Gonzales
