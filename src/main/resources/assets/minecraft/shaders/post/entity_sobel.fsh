#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec4 center = texture(InSampler, texCoord);
    
    
    if (center.a > 0.0) {
        fragColor = vec4(center.rgb, 0.03); 
        return;
    }

    
    vec4 sum = vec4(0.0);
    float count = 0.0;

    vec2 offsets[4] = vec2[](
        vec2(-1.0, -1.0),
        vec2(-1.0,  1.0),
        vec2( 1.0,  1.0),
        vec2( 1.0, -1.0)
    );

    for (int i = 0; i < 4; i++) {
        vec4 sampleColor = texture(InSampler, texCoord + oneTexel * offsets[i]);
        if (sampleColor.a > 0.0) {
            sum += sampleColor;
            count += 1.0;
        }
    }

    if (count > 0.0) {
        vec4 average = sum / count;
        fragColor = vec4(average.rgb, 0.4); 
    } else {
        fragColor = vec4(0.0); 
    }
}