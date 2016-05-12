package jp.co.cyberagent.android.gpuimage;

import android.opengl.GLES20;

/**
 * Created by CPU10924-local on 5/11/2016.
 */
public abstract class GPUTextCaptionFilter  {
    private int programHandle;
    private int vertexShaderHandle;
    private int fragmentShaderHandle;
    private boolean mInitialized;
    public GPUTextCaptionFilter()
    {
        mInitialized = false;
    }
    public void init()
    {
        init(null,null,null);
    }

    public void init(String vertextShaerCode, String fragmentShaderCode,AttribVariable[] programVariables) {
        vertexShaderHandle = Utilities.loadShader(GLES20.GL_VERTEX_SHADER, vertextShaerCode);
        fragmentShaderHandle = Utilities.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        programHandle = Utilities.createProgram(
                vertexShaderHandle, fragmentShaderHandle, programVariables);
        mInitialized = true;
    }
    public int getHandle()
    {
        return programHandle;
    }
    public void delete()
    {
        GLES20.glDeleteShader(vertexShaderHandle);
        GLES20.glDeleteShader(fragmentShaderHandle);
        GLES20.glDeleteProgram(programHandle);
        mInitialized = false;
    }
    public boolean initialized(){
        return mInitialized;
    }

}
