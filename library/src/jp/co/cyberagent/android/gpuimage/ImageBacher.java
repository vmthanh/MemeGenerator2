package jp.co.cyberagent.android.gpuimage;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CPU10924-local on 5/13/2016.
 */
public class ImageBacher {


    // Store the model matrix. This matrix is used to move models from object space (where each model can be thought
    // of being located at the center of the universe) to world space.

    // Store the projection matrix. This is used to project the scene onto a 2D viewport.
    private static  float[] mProjMatrix = new float[16];
    private static float[] modelMatrix = new float[16];
    // Allocate storage for the final combined matrix. This will be passed into the shader program.
    private float[] matrixMVP = new float[16];

    // Create our UV coordinates.
    static float[] uvArray = new float[]{
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    static FloatBuffer uvBuffer;
    static FloatBuffer vertexBuffer;
    static boolean initialized = false;
    static short[] indices = new short[]{0, 1, 2, 0, 2, 3}; // The order of vertexrendering.
    static ShortBuffer indicesBuffer;

    private List<ImageSprite> imageSpriteList = new ArrayList<>();

    public void begin() {
        int[] textureToDelete =new int[1];
        for(int i=0; i<imageSpriteList.size();++i)
        {
            int id = imageSpriteList.get(i).getTextureId();
            Log.v("Texture id delete:",String.valueOf(id));
            textureToDelete[0] = imageSpriteList.get(i).getTextureId();
            GLES20.glDeleteTextures(1,textureToDelete,0);
        }
        imageSpriteList.clear();

    }

    public ImageBacher()
    {
        if (!initialized)
        {
            // The texture buffer
            uvBuffer = ByteBuffer.allocateDirect(uvArray.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            uvBuffer.put(uvArray)
                    .position(0);

            // initialize byte buffer for the draw list
            indicesBuffer = ByteBuffer.allocateDirect(indices.length * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
            indicesBuffer.put(indices)
                    .position(0);

            float[] vertices = new float[] {
                    0, 0, 0,
                    0, 1, 0,
                    1, 1, 0,
                    1, 0, 0
            };

            // The vertex buffer.
            vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vertexBuffer.put(vertices)
                    .position(0);

            initialized = true;
        }
    }


    public void setScreenDimension(int width, int height)
    {
        Matrix.setIdentityM(mProjMatrix, 0);
        // (0,0)--->
        //   |
        //   v
        //I want it to be more natural like desktop screen
        Matrix.orthoM(mProjMatrix, 0,-1f, width, height, -1f,  -1f, 1f);
    }


    public void drawSticker(Sticker sticker) {
        ImageSprite  imageSprite = ImageSprite.createGLSprite(sticker);
        imageSpriteList.add(imageSprite);
    }

    public void end() {
        // Get handle to shape's transformation matrix
        int u_MVPMatrix = GLES20.glGetUniformLocation(ShaderImageHelper.programTexture, "u_MVPMatrix");
        int a_Position = GLES20.glGetAttribLocation(ShaderImageHelper.programTexture, "a_Position");
        int a_texCoord = GLES20.glGetAttribLocation(ShaderImageHelper.programTexture, "a_texCoord");
        int u_texture = GLES20.glGetUniformLocation(ShaderImageHelper.programTexture, "u_texture");

        GLES20.glEnableVertexAttribArray(a_Position);
        GLES20.glEnableVertexAttribArray(a_texCoord);
      //  Matrix.multiplyMM(matrixMVP, 0, mProjMatrix, 0, mVMatrix, 0);
        for(int i=0; i<imageSpriteList.size();++i)
        {


            ImageSprite imageSprite = imageSpriteList.get(i);
            // Matrix op - start
            Matrix.setIdentityM(matrixMVP,0);
            Matrix.setIdentityM(modelMatrix, 0);



            Matrix.rotateM(modelMatrix,0,-myRotation,0,0,1.0f);
            Matrix.translateM(modelMatrix, 0, imageSprite.getX() ,imageSprite.getY() , 0f);
            Matrix.scaleM(modelMatrix, 0, imageSprite.getWidth()*imageSprite.getScale(), imageSprite.getHeight()*imageSprite.getScale(), 0f);

            Matrix.multiplyMM(matrixMVP, 0,modelMatrix, 0, matrixMVP, 0);
            Matrix.multiplyMM(matrixMVP,0,mProjMatrix,0,matrixMVP,0);
            // Matrix op - end
            GLES20.glVertexAttribPointer(a_Position, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

            // Prepare the texturecoordinates
            GLES20.glVertexAttribPointer(a_texCoord, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);

            GLES20.glUniformMatrix4fv(u_MVPMatrix,1,true,matrixMVP,0);

            GLES20.glUniform1i(u_texture,0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imageSprite.getTextureId());

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, indicesBuffer);


            Matrix.setIdentityM(matrixMVP,0);
            GLES20.glUniformMatrix4fv(u_MVPMatrix,1,false,matrixMVP,0);


        }
    }
    private int myRotation = 0;
    public void setMyRotation(int rotation)
    {
        this.myRotation = rotation;
    }
}
