//
// Created by 帅潇杰 on 2024/10/25.
//

#ifndef EHVIEWER_IMAGE_WEBP_H
#define EHVIEWER_IMAGE_WEBP_H


#include "config.h"
#ifdef IMAGE_SUPPORT_WEBP

#include <stdbool.h>


#include "../utils.h"
#include "../stream/patch_head_input_stream.h"
#include "../libwebp/examples/anim_util.h"
#include "../libwebp/src/webp/mux_types.h"

#define IMAGE_WEBP_DECODER_DESCRIPTION ("libwebp-rfc9649")

#define IMAGE_WEBP_MAGIC_NUMBER_0 0x52
#define IMAGE_WEBP_MAGIC_NUMBER_1 0x49
#define IMAGE_WEBP_MAGIC_NUMBER_00 0x57
#define IMAGE_WEBP_MAGIC_NUMBER_11 0x45

typedef enum {
    WEBP_INFO_OK = 0,
    WEBP_INFO_TRUNCATED_DATA,
    WEBP_INFO_PARSE_ERROR,
    WEBP_INFO_INVALID_PARAM,
    WEBP_INFO_BITSTREAM_ERROR,
    WEBP_INFO_MISSING_DATA,
    WEBP_INFO_INVALID_COMMAND
} WebPInfoStatus;

typedef enum ChunkID {
    CHUNK_VP8,
    CHUNK_VP8L,
    CHUNK_VP8X,
    CHUNK_ALPHA,
    CHUNK_ANIM,
    CHUNK_ANMF,
    CHUNK_ICCP,
    CHUNK_EXIF,
    CHUNK_XMP,
    CHUNK_UNKNOWN,
    CHUNK_TYPES = CHUNK_UNKNOWN
} ChunkID;

typedef struct
{
    AnimatedImage info;
    void* buffer;
    int buffer_index;
    void* backup;
    bool partially;
    // Use extra buffer to avoid blink
    WebPData webp_data;
    PatchHeadInputStream* patch_head_input_stream;
} WEBP;

void* WEBP_decode(JNIEnv* env, PatchHeadInputStream* patch_head_input_stream, bool partially);
bool WEBP_complete(JNIEnv* env, WEBP* WEBP);
bool WEBP_is_completed(WEBP* webp);
void* WEBP_get_pixels(WEBP* webp);
int WEBP_get_width(WEBP* webp);
int WEBP_get_height(WEBP* webp);
int WEBP_get_byte_count(WEBP* webp);
void WEBP_render(WEBP* webp, int src_x, int src_y,
                void* dst, int dst_w, int dst_h, int dst_x, int dst_y,
                int width, int height, bool fill_blank, int default_color);
void WEBP_advance(WEBP* webp);
int WEBP_get_delay(WEBP* webp);
int WEBP_get_frame_count(WEBP* webp);
bool WEBP_is_opaque(WEBP* webp);
void WEBP_recycle(JNIEnv* env, WEBP* webp);


#endif // IMAGE_SUPPORT_WEBP

#endif //EHVIEWER_IMAGE_WEBP_H