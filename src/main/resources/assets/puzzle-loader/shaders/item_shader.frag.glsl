#version 150
#ifdef GL_ES
precision mediump float;
#endif

in vec2 v_texCoord0;
in vec3 v_normal;

uniform sampler2D texDiffuse;
uniform vec4 tintColor;

out vec4 outColor;

void main()
{
    //bs numbers might want to mess around with
    float test = abs(dot(vec3(0,0,1), v_normal) ) + 0.6;
    test *= abs(dot(vec3(0,1,0), v_normal) + 0.8);
    test *= 1.5;
    vec4 texColor = texture(texDiffuse, v_texCoord0);

    if(texColor.a == 0)
    {
        discard;
    }

    outColor = vec4(texColor.rgb * test , texColor.a) * tintColor;
}