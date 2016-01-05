package com.yanbin.sticky.widgest;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

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
    //两圆最远距离
    private float farestDistance =80f;
    private boolean isOutOfRange;
    private boolean isDisappear;

    public GooView(Context context) {
        this(context,null);
    }

    public GooView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化一个抗锯齿的画笔
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
       // int actionBarHeight =Context.getActionBar().getHeight();
    }
    //定义回调
    public interface OnDragChangedListener{
        void onDisappear();
        void onReset(boolean isOutofRange);
    }
    private OnDragChangedListener onDragChangedListener;

    public OnDragChangedListener getOnDragChangedListener() {
        return onDragChangedListener;
    }

    public void setOnDragChangedListener(OnDragChangedListener onDragChangedListener) {
        this.onDragChangedListener = onDragChangedListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            // 计算变量的值

            // 0. 求出实时的固定圆半径
                float tempStickyCenterRadius=getStickRadius();
                mStickRadius=tempStickyCenterRadius;

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
        canvas.translate(0, -statusBarHeight);

            //画最大范围的参考圆环
        mPaint.setStyle(Paint.Style.STROKE);//只画圆环
        canvas.drawCircle(mStickCenter.x, mStickCenter.y, farestDistance, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        if(!isDisappear) {//只有没有消失才绘制
            //如果没有超出范围 画固定圆和连接部分
            if (!isOutOfRange) {
                //画中间连接部分
                Path path = new Path();
                //贝塞尔曲线 指定开始节点，结束节点，和控制点
                //结合图片看
                //跳到点1  替换为定义的变量
                path.moveTo(mStickPoints[0].x, mStickPoints[0].y);
                //1-->2
                //指定控制点 和 结束点 画曲线
                path.quadTo(mControlPoint.x, mControlPoint.y, mDragPoints[0].x, mDragPoints[0].y);
                //2->3
                path.lineTo(mDragPoints[1].x, mDragPoints[1].y);
                //3--》4
                path.quadTo(mControlPoint.x, mControlPoint.y, mStickPoints[1].x, mStickPoints[1].y);
                //二阶贝塞尔曲线
//        path.cubicTo();
                //会自动封闭
                path.close();//将图形封闭

                canvas.drawPath(path, mPaint);
                //画固定圆
                canvas.drawCircle(mStickCenter.x, mStickCenter.y, mStickRadius, mPaint);

                // 画附着点(参考用)
                mPaint.setColor(Color.BLUE);
                canvas.drawCircle(mDragPoints[0].x, mDragPoints[0].y, 3f, mPaint);
                canvas.drawCircle(mDragPoints[1].x, mDragPoints[1].y, 3f, mPaint);
                canvas.drawCircle(mStickPoints[0].x, mStickPoints[0].y, 3f, mPaint);
                canvas.drawCircle(mStickPoints[1].x, mStickPoints[1].y, 3f, mPaint);
                mPaint.setColor(Color.RED);
            }

            //画拖拽圆
            canvas.drawCircle(mDragCenter.x, mDragCenter.y, mDragRadius, mPaint);

        }


        canvas.restore();//恢复为之前保存的状态
    }

    /**
     * 获取固定圆变化的半径
     * @return
     */
    private float getStickRadius() {
        //开始距离
        float distanceBetween2Points = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
        //0--》farest 80f
        distanceBetween2Points=Math.min(distanceBetween2Points, farestDistance);
        //0.0f -> 1.0f (12f->4f ) 固定圆半径由最大逐渐
        float percent=distanceBetween2Points/ farestDistance;
        Float stickyRadius = evaluate(percent, 12f, 4f);
        return  stickyRadius;
    }
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rawX;
        float rawY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDisappear = false;
                isOutOfRange = false;

                rawX = event.getRawX();
                rawY = event.getRawY();
                updateDragCenter(rawX, rawY);

                break;
            case MotionEvent.ACTION_MOVE:
                rawX = event.getRawX();
                rawY = event.getRawY();
                updateDragCenter(rawX, rawY);
//
                // 拖拽过程中 判断两圆心距离
                float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
                if(distance > farestDistance){
                    isOutOfRange  = true;
                    invalidate();

//                    if(onDragChangedListener!=null)
//                        onDragChangedListener.onDisappear();

                }

                break;
            case MotionEvent.ACTION_UP:

                if(isOutOfRange){
                    // 刚刚超出范围了 松手再去获取两圆距离
                    float d = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
                    if(d > farestDistance){
                        // 1.松手时还在圈外 松手时消失
                        isDisappear  = true;
                        invalidate();

                        if(onDragChangedListener!=null)
                            onDragChangedListener.onDisappear();

                    }else {
//                        // 2.松手时放回圈里
                        updateDragCenter(mStickCenter.x, mStickCenter.y);
//
                        if(onDragChangedListener!=null)
                            onDragChangedListener.onReset(true);
                    }
                }else {
                    // 松手时没超出范围, 弹回去 使用动画
                    //弹回去意思是 拖拽圆逐渐回弹到固定圆 根据动画执行时间不断改变拖拽圆圆心 绘制界面
                    //开始point
                    final PointF startP = new PointF(mDragCenter.x, mDragCenter.y);
//
                    ValueAnimator anim = ValueAnimator.ofFloat(10f);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
                        @Override
                        public void onAnimationUpdate(ValueAnimator mAnim) {
                            Float animatedValue = (Float) mAnim.getAnimatedValue();
                            //动画从percent
                            float percent = mAnim.getAnimatedFraction();
                            // 0.0f -> 1.0f
                            System.out.println("percent: " + percent + " animatedValue: " + animatedValue);
                            // 根据百分比获取从 拖拽圆圆心到固定圆圆心的中间某个点坐标
                            //开始点x不断接近结束点x 开始点y不断接近结束点y
                            PointF p = GeometryUtil.getPointByPercent(startP, mStickCenter, percent);

                            updateDragCenter(p.x, p.y);
                        }
                    });
                    //Simple , Base, Default, Adapter, Basic
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //动画执行完毕执行回调
                            if(onDragChangedListener!=null)
                                onDragChangedListener.onReset(false);
                        }
                    });

                    //OvershootInterpolator 类似于皮筋的张力
                    anim.setInterpolator(new OvershootInterpolator(2));
                    anim.setDuration(300);
                    anim.start();
                }

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
