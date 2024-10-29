//
// Created by 帅潇杰 on 2024/10/25.
//
#include "config.h"

#ifdef IMAGE_SUPPORT_WEBP

#include <stdlib.h>
#include <string.h>

#include "patch_head_input_stream.h"
#include "image_utils.h"
#include "java_wrapper.h"
#include "../log.h"
#include "../utils.h"
#include "image_webp.h"
#include "../libwebp/src/webp/demux.h"
#include "../libwebp/examples/unicode.h"
#include "../libwebp/imageio/imageio_util.h"
#include "../libwebp/examples/anim_util.c"

int ImgIoUtilReadStream(JNIEnv *env, PatchHeadInputStream *patch_head_input_stream,
                        const uint8_t **data, size_t *data_size) {
    size_t file_size;
    uint8_t *file_data;
    file_data = read_patch_head_input_stream_all(env, patch_head_input_stream, &file_size);
    *data = file_data;
    *data_size = file_size;
    return 1;
}


void *WEBP_decode(JNIEnv *env, PatchHeadInputStream *patch_head_input_stream, bool partially) {
    WEBP *webp = NULL;
    webp = (WEBP *) malloc(sizeof(WEBP));
    WebPData webp_data;
    WebPDataInit(&webp_data);

    ImgIoUtilReadStream(env, patch_head_input_stream, &webp_data.bytes, &webp_data.size);

    uint32_t frame_index = 0;
    int prev_frame_timestamp = 0;
    WebPAnimDecoder *dec;
    AnimatedImage* image;
    WebPAnimInfo anim_info;
    memset(&image, 0, sizeof(*image));

    if (!IsWebP(&webp_data)) {
        free(webp);
        close_patch_head_input_stream(env, patch_head_input_stream);
        return NULL;
    }


    dec = WebPAnimDecoderNew(&webp_data, NULL);
    if (dec == NULL) {
        WFPRINTF(stderr, "Error parsing image: %s\n", (const W_CHAR*)&patch_head_input_stream);
        free(webp);
        close_patch_head_input_stream(env, patch_head_input_stream);
        return NULL;
    }
    if (!WebPAnimDecoderGetInfo(dec, &anim_info)) {
        fprintf(stderr, "Error getting global info about the animation\n");
        free(webp);
        close_patch_head_input_stream(env, patch_head_input_stream);
        return NULL;
    }

    // Animation properties.
    image->canvas_width = anim_info.canvas_width;
    image->canvas_height = anim_info.canvas_height;
    image->loop_count = anim_info.loop_count;
    image->bgcolor = anim_info.bgcolor;
    // Allocate frames.
    if (!AllocateFrames(image, anim_info.frame_count)){
        close_patch_head_input_stream(env, patch_head_input_stream);
        free(webp);
        return NULL;
    }

    // Decode frames.
    while (WebPAnimDecoderHasMoreFrames(dec)) {
        DecodedFrame* curr_frame;
        uint8_t* curr_rgba;
        uint8_t* frame_rgba;
        int timestamp;

        if (!WebPAnimDecoderGetNext(dec, &frame_rgba, &timestamp)) {
            fprintf(stderr, "Error decoding frame #%u\n", frame_index);
            close_patch_head_input_stream(env, patch_head_input_stream);
            free(webp);
            return NULL;
        }
        assert(frame_index < anim_info.frame_count);
        curr_frame = &image->frames[frame_index];
        curr_rgba = curr_frame->rgba;
        curr_frame->duration = timestamp - prev_frame_timestamp;
        curr_frame->is_key_frame = 0;  // Unused.
        memcpy(curr_rgba, frame_rgba,
               image->canvas_width * kNumChannels * image->canvas_height);

        // Needed only because we may want to compare with GIF later.
        CleanupTransparentPixels((uint32_t*)curr_rgba,
                                 image->canvas_width, image->canvas_height);

        ++frame_index;
        prev_frame_timestamp = timestamp;
    }
    webp->partially = partially;
    webp->patch_head_input_stream = patch_head_input_stream;

    return webp;
}


#endif // IMAGE_SUPPORT_WEBP
