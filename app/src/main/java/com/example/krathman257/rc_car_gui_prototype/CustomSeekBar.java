/* RC Car, Team G.2
 * Custom Seekbar
 * Written by Fudge, Modified by Kyle Rathman
 */

package com.example.krathman257.rc_car_gui_prototype;

//Import necessary packages
import android.annotation.SuppressLint;
import android.graphics.LinearGradient;
import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Shader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.SeekBar;
import android.graphics.Rect;

@SuppressLint("AppCompatCustomView")
public class CustomSeekBar extends SeekBar {

    //Declare variables
    private int progress1 = Color.argb(255, 150, 150, 150);
    private int progress2 = Color.argb(255, 0, 0, 0);
    private Shader shader = new LinearGradient(0, 0, 0, 100, progress1, progress2, Shader.TileMode.CLAMP);
    private int seekbar_height;
    private Paint paint;
    private Rect rect;

    public CustomSeekBar(Context context) {
        super(context);
    }

    //Create the seekbar
    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new Rect();
        paint = new Paint();
        seekbar_height = 30;
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        int maxval = 160;

        //If the seekbar is positive, draw the 'progress' up
        if (this.getProgress() > maxval / 2) {
            rect.set(getWidth() / 2,
                    (getHeight() / 2) - (seekbar_height/2),
                    (int) (getWidth() / 2 + (getWidth() / maxval) * (getProgress() - maxval / 2) * 1.6),
                    getHeight() / 2 + (seekbar_height/2));

            paint.setShader(shader);
            canvas.drawRect(rect, paint);
        }

        //If the seekbar is negative draw the 'progress' down
        else if (this.getProgress() < maxval / 2) {
            rect.set((int) (getWidth() / 2 - (getWidth() / maxval) * (maxval / 2 - getProgress()) * 1.6),
                    (getHeight() / 2) - (seekbar_height/2),
                    getWidth() / 2,
                    getHeight() / 2 + (seekbar_height/2));

            paint.setShader(shader);
            canvas.drawRect(rect, paint);
        }

        //Draw the progress
        super.onDraw(canvas);
    }
}