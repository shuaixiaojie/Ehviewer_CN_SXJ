cmake_minimum_required(VERSION 3.14)
project(libwebp C)

set(LIBWEBP_SOURCES
        src/webp/decode.h
        src/webp/demux.h
        src/webp/encode.h
        src/webp/format_constants.h
        src/webp/mux.h
        src/webp/mux_types.h
        src/webp/types.h
        src/utils/utils.h
        examples/unicode.h
        examples/anim_util.c
        imageio/imageio_util.h
)
set(LIBWEBP_INCLUDES ${CMAKE_CURRENT_SOURCE_DIR}/src)

add_library(libwebp STATIC ${LIBWEBP_SOURCES})
target_include_directories(libwebp PUBLIC ${LIBWEBP_INCLUDES})

set_target_properties(libwebp PROPERTIES LINKER_LANGUAGE CXX)