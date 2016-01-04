package com.yanbin.sticky.widgest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yanbin.sticky.utils.GeometryUtil;
import com.yanbin.sticky.utils.Utils;

/**
 * 粘性控件
 * 仿照 qq 未读消息
 *
 * Created by yanbin on 2016/1/4.
 *
 */
public class GooView extends View{

    Paint mPaint;
    //看图 拖拽圆圆心
    PointF mDragCenter = new PointF(80f, 80f);
    float mDragRadius = 16f;
    PointF mStickCenter = new PointF(150f, 150f);
    float mStickRadius = 12f;

    //固定圆附着点
    PointF[] mStickPoints = new PointF[]{
            new PointF(250f, 250f),
            new PointF(250f, 350f)
    };
    //拖拽圆附着点
    PointF[] mDragPoints = new PointF[]{
            new PointF(50f, 250f),
            new PointF(50f, 350f)
    };

    PointF mControlPoint = new PointF(150f, 300f);

    public GooView(Context context) {
        this(context,null);
    }

    public GooView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化一个抗锯齿的画笔
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
       // int actionBarHeight =Context.getActionBar().getHeight();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            // 计算变量的值

            // 0. 求出实时的固定圆半径
        // 1. 四个点附着点的坐标
        float yOffset=mDragCenter.y-mStickCenter.y;
        float xOffset=mDragCenter.x-mStickCenter.x;
        Double linek=null;
        if(xOffset!=0){
            //除数不能为0
            linek=(double)yOffset/xOffset;
        }
        mStickPoints= GeometryUtil.getIntersectionPoints(mStickCenter, mStickRadius, linek);
        mDragPoints = GeometryUtil.getIntersectionPoints(mDragCenter, mDragRadius, linek);

        //2.0控制点坐标
        mControlPoint=GeometryUtil.getMiddlePoint(mDragCenter,mStickCenter);

        // 保存当前状态, 向上平移状态栏的高度, 以使其与手指按下点重合
        canvas.save();
        canvas.translate(0,-statusBarHeight);

        //画中间连接部分
        Path path=new Path();
        //贝塞尔曲线 指定开始节点，结束节点，和控制点
        //结合图片看
        //跳到点1  替换为定义的变量
        path.moveTo(mStickPoints[0].x, mStickPoints[0].y);
        //1-->2
        //指定控制点 和 结束点 画曲线
        path.quadTo(mControlPoint.x,mControlPoint.y,mDragPoints[0].x,mDragPoints[0].y);
        //2->3
        path.lineTo(mDragPoints[1].x, mDragPoints[1].y);
        //3--》4
        path.quadTo(mControlPoint.x,mControlPoint.y,mStickPoints[1].x,mStickPoints[1].y);
        //二阶贝塞尔曲线
//        path.cubicTo();
        //会自动封闭
        path.close();//将图形封闭

        canvas.drawPath(path, mPaint);

        // 画附着点(参考用)
        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(mDragPoints[0].x, mDragPoints[0].y, 3f, mPaint);
        canvas.drawCircle(mDragPoints[1].x, mDragPoints[1].y, 3f, mPaint);
        canvas.drawCircle(mStickPoints[0].x, mStickPoints[0].y, 3f, mPaint);
        canvas.drawCircle(mStickPoints[1].x, mStickPoints[1].y, 3f, mPaint);
        mPaint.setColor(Color.RED);



        //画拖拽圆
        canvas.drawCircle(mDragCenter.x,mDragCenter.y,mDragRadius,mPaint);

        //画固定圆
        canvas.drawCircle(mStickCenter.x,mStickCenter.y,mStickRadius,mPaint);


        canvas.restore();//恢复为之前保存的状态
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rawX;
        float rawY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                isDisappear = false;
//                isOutOfRange = false;

                rawX = event.getRawX();
                rawY = event.getRawY();
                updateDragCenter(rawX, rawY);

                break;
            case MotionEvent.ACTION_MOVE:
                rawX = event.getRawX();
                rawY = event.getRawY();
                updateDragCenter(rawX, rawY);
//
//                // 拖拽过程中 判断两圆心距离
//                float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
//                if(distance > farestDistance){
//                    isOutOfRange  = true;
                    invalidate();
//                }

                break;
            case MotionEvent.ACTION_UP:

//                if(isOutOfRange){
//                    // 刚刚超出范围了
//                    float d = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
//                    if(d > farestDistance){
//                        // 1.松手时还在圈外 消失
//                        isDisappear  = true;
//                        invalidate();
//
//                        if(onDragChangeListener != null){
//                            onDragChangeListener.onDisappear();
//                        }
//
//                    }else {
//                        // 2.松手时放回圈里
//                        updateDragCenter(mStickCenter.x, mStickCenter.y);
//
//                        if(onDragChangeListener != null){
//                            onDragChangeListener.onReset(true);
//                        }
//                    }
//                }else {
//                    // 松手时没超出范围, 弹回去
//
//                    final PointF startP = new PointF(mDragCenter.x, mDragCenter.y);
//
//                    ValueAnimator anim = ValueAnimator.ofFloat(10f);
//                    anim.addUpdateListener(new AnimatorUpdateListener() {
//
//                        @Override
//                        public void onAnimationUpdate(ValueAnimator mAnim) {
//                            Float animatedValue = (Float) mAnim.getAnimatedValue();
//                            float percent = mAnim.getAnimatedFraction();
//                            // 0.0f -> 1.0f
//                            System.out.println("percent: " + percent + " animatedValue: " + animatedValue);
//                            // 根据百分比获取从 拖拽圆圆心到固定圆圆心的中间某个点坐标
//                            PointF p = GeometryUtil.getPointByPercent(startP, mStickCenter, percent);
//
//                            updateDragCenter(p.x, p.y);
//                        }
//                    });
//                    anim.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//
//                            if(onDragChangeListener != null){
//                                onDragChangeListener.onReset(false);
//                            }
//                        }
//                    });
//                    //Simple , Base, Default, Adapter, Basic
//                    anim.setInterpolator(new OvershootInterpolator(2));
//                    anim.setDuration(300);
//                    anim.start();
//                }

                break;

            default:
                break;
        }

        return true;
    }

    /**
     * 根据拖拽圆心绘制
     * @param rawX
     * @param rawY
     */
    private void updateDragCenter(float rawX, float rawY) {
        mDragCenter.set(rawX,rawY);
        invalidate();
    }

    int statusBarHeight;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        statusBarHeight = Utils.getStatusBarHeight(this);

    }
}
