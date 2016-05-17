package jp.co.cyberagent.android.gpuimage;

import android.opengl.GLES20;

/**
 * Created by CPU10924-local on 5/13/2016.
 */
public class ShaderImageHelper {
    static final String vs_Image =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    static final String fs_Image =
            "precision mediump float;" +
                    "uniform sampler2D u_texture;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(u_texture, v_texCoord);" +
                    "}";

    public static int programTexture;
    public static int vertexShaderImage, fragmentShaderImage;
    public static int loadShader(int type, String shaderCode)
    {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader,shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public  static void initGlProgram()
    {
        vertexShaderImage = ShaderImageHelper.loadShader(GLES20.GL_VERTEX_SHADER,ShaderImageHelper.vs_Image);
        fragmentShaderImage = ShaderImageHelper.loadShader(GLES20.GL_FRAGMENT_SHADER,ShaderImageHelper.fs_Image);

        ShaderImageHelper.programTexture = GLES20.glCreateProgram();
        GLES20.glAttachShader(ShaderImageHelper.programTexture, vertexShaderImage);   // add the vertex shader to program
        GLES20.glAttachShader(ShaderImageHelper.programTexture, fragmentShaderImage); // add the fragment shader to program
        GLES20.glLinkProgram(ShaderImageHelper.programTexture);                  // creates OpenGL ES program executables
    }

    public static void dispose() {
        GLES20.glDetachShader(ShaderImageHelper.programTexture, ShaderImageHelper.vertexShaderImage);
        GLES20.glDetachShader(ShaderImageHelper.programTexture, ShaderImageHelper.fragmentShaderImage);

        GLES20.glDeleteShader(ShaderImageHelper.fragmentShaderImage);
        GLES20.glDeleteShader(ShaderImageHelper.vertexShaderImage);

        GLES20.glDeleteProgram(ShaderImageHelper.programTexture);
    }
}
