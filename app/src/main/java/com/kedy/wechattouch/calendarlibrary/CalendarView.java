package com.kedy.wechattouch.calendarlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.solver.widgets.WidgetContainer;

import com.kedy.wechattouch.R;

import java.util.ArrayList;
import java.util.List;

public class CalendarView extends View {
    private static final String TAG = "CalendarView";

    /** 单个Utils实例*/
    private CalendarUtils mCalendarUtils;
    private int mMaxHeightDp;
    private int mMaxWidthDp;

    /** 各部分背景*/
    private int mTitleBg, mWeekBg, mDayBg;
    /** 标题的颜色、大小、高度*/
    private String mTitleText;
    private int mTitleTextColor;
    private int mTitleTextSize;
    private int mTitleSpace;
    private int mTitleHeight;
    /** 标题的箭头、距离*/
    private int mTitleArrowLeft, mTitleArrowRight;
    private int mTitleArrowSpace;
    /** 星期的颜色、大小、高度*/
    private int mWeekTextColor;
    private int mWeekTextSize;
    private int mWeekHeight;
    /** 日期文本的颜色、大小、高度*/
    private int mDayTextColor;
    private int mDayTextSize;
    private int mDayHeight;
    /** 任务图标的大小，设定为边长相等的正方形，所以在onMeasure的时候根据父件的宽窄来定义*/
    private int mIconSize;
    /** 选中的日期的颜色*/
    private int mSelectedMonth = 3;
    private int mSelectedDay = 20;
    private int mSelectedDayColor;
    /** 选中背景*/
    private int mSelectedDayBg, mCurrentDayBg;
    private int mSelectedRadius;
    private float[] mCurrentDayDashPath;
    /** 线条粗细*/
    private int mStrokeWidth;

    /** 行间距、高度*/
    private int mDaySpace;
    private int mRowHeight;
    /** 字体上下间距*/
    private int mTextSpace;

    /** 日历组件位置定义*/
    private RectF mViewRectF;
    private RectF mTitleRectF;
    private RectF mTitleTextRectF;
    private RectF mLastRectF;
    private RectF mNextRectF;
    private RectF mDayContentRectF;
    private List<RectF> mCurrentMonthDayRectFs;
    private List<RectF> mNextMonthDayRectFs;
    private List<RectF> mLastMonthDayRectFs;
    private List<RectF> mWeekRectFs;

    private List<DayBox> mCurrentMonthDayBoxes;
    private List<DayBox> mNextMonthDayBoxes;
    private List<DayBox> mLastMonthDayBoxes;

    private int mCurrentDay;
    private int mCurrentMonth;
    private int mCurrentYear;

    private Paint mStrokePaint;
    private Paint mbgPaint;
    private Paint mTextPaint;

    private int mTouchSlop;
    private float mOffsetX;
    private float mOffsetY;
    private Type mScrollToType;

    private Scroller mContentScroller;
    private VelocityTracker mVelocityTracker;
    boolean mIsDrawing;

    private Context mContext;

    enum Type {
        /**
         * 上个月，下个月，当前月
         */
        LAST,
        NEXT,
        /**
         * 上一年，下一年
         */
        LAST_YEAR,
        NEXT_YEAR,
        NONE
    }

    enum ScrollOrientation {
        /**
         * 滑动方向
         */
        Vertical,
        Horizontal,
        None
    }

    public CalendarView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //宽度 = 填充父窗体
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);   //获取宽的尺寸
        //每行高度 += 图标高度（每一列最多3个图标）
//        mIconSize = widthSize / CalendarUtils.NUM_COL / 3;
        mIconSize = 20;
        mRowHeight += mIconSize;
        //高度 = 标题高度 + 星期高度 + 日期行数*每行高度
        int height = mTitleHeight + mWeekHeight + (CalendarUtils.NUM_ROW * mRowHeight);
        Log.v(TAG, "标题高度："+mTitleHeight+" 星期高度："+mWeekHeight+" 每行高度："+mRowHeight+
                "  \n控件高度："+height);
        computeRectF(widthSize, height);
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mIsDrawing = true;
        drawBackground(canvas);
        drawTitle(canvas);
        drawTitleArrow(canvas);
        drawWeek(canvas);
        drawAllDay(canvas);
        drawOuterLine(canvas);
        mIsDrawing = false;
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        mCalendarUtils = CalendarUtils.getInstance();
        int[] maxDp = mCalendarUtils.setScale(context.getResources().getDisplayMetrics());
        mMaxWidthDp = maxDp[0];
        mMaxHeightDp = maxDp[1];
        Log.d(TAG, "maxWidthDp: " + mMaxWidthDp + " maxHeightDp: " + mMaxHeightDp);

        initAttrs(attrs, defStyleAttr);

        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mContentScroller = new Scroller(context);
        mVelocityTracker = VelocityTracker.obtain();

        initPaint();
        initSize();
        initRectF();

        // Initialize to current date
        int [] date = mCalendarUtils.getCurrentDate();
        mCurrentYear = date[0];
        mCurrentMonth = date[1];
        mCurrentDay = date[2];
        mTitleText = mCurrentYear + "年" + mCurrentMonth + "月";
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        //获取自定义属性的值
        TypedArray a = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, 0);

        mTitleBg = a.getColor(R.styleable.CalendarView_mBgMonth, Color.TRANSPARENT);
        mWeekBg = a.getColor(R.styleable.CalendarView_mBgWeek, Color.TRANSPARENT);
        mDayBg = a.getColor(R.styleable.CalendarView_mBgDay, Color.TRANSPARENT);

        mTitleTextColor = a.getColor(R.styleable.CalendarView_mTitleTextColor, Color.BLACK);
        mWeekTextColor = a.getColor(R.styleable.CalendarView_mWeekTextColor, Color.BLACK);
        mDayTextColor = a.getColor(R.styleable.CalendarView_mDayTextColor, Color.BLACK);

        mTitleTextSize = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mTitleTextSize, 25));
        mWeekTextSize = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mTitleTextSize, 15));
        mDayTextSize = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mDayTextSize, 15));

        mTitleArrowLeft = a.getResourceId(R.styleable.CalendarView_mTitleArrowLeft, R.drawable.calendar_arrow_left);
        mTitleArrowRight = a.getResourceId(R.styleable.CalendarView_mTitleArrowRight, R.drawable.calendar_arrow_right);
        mTitleArrowSpace = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mTitleArrowSpace, 20));

        mSelectedDayColor = a.getColor(R.styleable.CalendarView_mSelectedDayColor, Color.BLACK);
        mSelectedDayBg =  a.getColor(R.styleable.CalendarView_mSelectedDayBg, Color.YELLOW);
        mSelectedRadius = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mSelectedRadius, 15));
        mCurrentDayBg = a.getColor(R.styleable.CalendarView_mCurrentDayBg, Color.GRAY);
        try {
            int dashPathId = a.getResourceId(R.styleable.CalendarView_mCurrentDayDashPath, R.array.calendar_currentDay_bg_DashPath);
            int[] array = getResources().getIntArray(dashPathId);
            mCurrentDayDashPath = new float[array.length];
            for(int i = 0; i < array.length; i++){
                mCurrentDayDashPath[i] = array[i];
            }
        }catch (Exception e){
            e.printStackTrace();
            mCurrentDayDashPath = new float[]{2, 3, 2, 3};
        }
        mStrokeWidth = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mStrokeWidth, 1));

        mTitleSpace = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mTitleSpace, 5));
        mDaySpace = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mDaySpace, 10));
        mTextSpace = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mTextSpac, 0));
        a.recycle();  //注意回收
    }

    private void initPaint() {
        //专门用来画线条
        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(mStrokeWidth);

        //专门用来画背景
        mbgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mbgPaint.setStyle(Paint.Style.FILL);

        //专门用来写字
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        //字体会从中间开始往两边拓展所以直接用RectF.centerX()就可以
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initSize() {
        //标题高度
        mTextPaint.setTextSize(mTitleTextSize);
        mTitleHeight = (int) getFontHeight(mTextPaint) + mTitleSpace;
        //星期高度
        mTextPaint.setTextSize(mWeekTextSize);
        mWeekHeight = (int) getFontHeight(mTextPaint);
        //日期高度
        mTextPaint.setTextSize(mDayTextSize);
        mDayHeight = (int) getFontHeight(mTextPaint);
        //每行高度 = 行间距 + 日期字体高度 + 字间距 + 图标高度（暂无，需要在onMeasure的时候构建）
        mRowHeight = mDaySpace + mDayHeight + mTextSpace;
    }

    private void initRectF() {
        mViewRectF = new RectF();
        mTitleRectF = new RectF();
        mTitleTextRectF = new RectF();
        mLastRectF = new RectF();
        mNextRectF = new RectF();
        mDayContentRectF = new RectF();

        mCurrentMonthDayRectFs = new ArrayList<>(CalendarUtils.NUM_CELL);
        mNextMonthDayRectFs = new ArrayList<>(CalendarUtils.NUM_CELL);
        mLastMonthDayRectFs = new ArrayList<>(CalendarUtils.NUM_CELL);

        mWeekRectFs = new ArrayList<>(CalendarUtils.NUM_COL);

        mCurrentMonthDayBoxes = new ArrayList<>(CalendarUtils.NUM_CELL);
        mNextMonthDayBoxes = new ArrayList<>(CalendarUtils.NUM_CELL);
        mLastMonthDayBoxes = new ArrayList<>(CalendarUtils.NUM_CELL);


        for (int index = 0; index < CalendarUtils.NUM_COL; index++) {
            mWeekRectFs.add(new RectF());
        }
        for (int index = 0; index < CalendarUtils.NUM_CELL; index++) {
            mCurrentMonthDayRectFs.add(new RectF());
            mNextMonthDayRectFs.add(new RectF());
            mLastMonthDayRectFs.add(new RectF());
        }
    }

    private void computeRectF(int viewWidth, int viewHeight) {
        Log.d(TAG, "总宽度: " + viewWidth + " 总高度: " + viewHeight);
        mViewRectF.set(mStrokeWidth, mStrokeWidth,
                viewWidth - mStrokeWidth, viewHeight - mStrokeWidth);
        int itemWidth = viewWidth / CalendarUtils.NUM_COL;

        //title 参数
        mTextPaint.setTextSize(mTitleTextSize);
        Log.d(TAG, "标题长度: " + getFontlength(mTextPaint, mTitleText));
        int titleStart = (viewWidth - (int) getFontlength(mTextPaint, mTitleText)) / 2;
        mTitleTextRectF.set(titleStart, 0,
                titleStart + (int) getFontlength(mTextPaint, mTitleText), mTitleHeight);
        mNextRectF.set(mTitleTextRectF.right, 0, mTitleTextRectF.right + mCalendarUtils.dip2px(mTitleArrowSpace), mTitleHeight);
        mLastRectF.set(mTitleTextRectF.left - mCalendarUtils.dip2px(mTitleArrowSpace), 0, mTitleTextRectF.left, mTitleHeight);
        mTitleRectF.set(0, 0, viewWidth, mTitleHeight);

        //week 参数
        for (int index = 0; index < CalendarUtils.NUM_COL; index++) {
            int startX = index * itemWidth + mStrokeWidth;
            mWeekRectFs.get(index).set(startX, mTitleHeight, startX + itemWidth - mStrokeWidth, mTitleHeight + mWeekHeight);
        }

        //day 参数
        for (int row = 0; row < CalendarUtils.NUM_ROW; row++) {
            int startY = mTitleHeight + mWeekHeight + row * mRowHeight;
            for (int column = 0; column < CalendarUtils.NUM_COL; column++) {
                int index = row * CalendarUtils.NUM_COL + column;
                int startX = column * itemWidth + (int) mViewRectF.left;
                RectF rectF = mCurrentMonthDayRectFs.get(index);
                rectF.set(startX, startY, startX + itemWidth - mStrokeWidth, startY + mRowHeight);
                mNextMonthDayRectFs.get(index).set(rectF.left + viewWidth, rectF.top, rectF.right + viewWidth, rectF.bottom);
                mLastMonthDayRectFs.get(index).set(rectF.left - viewWidth, rectF.top, rectF.right - viewWidth, rectF.bottom);
            }
        }
        mDayContentRectF.set(mCurrentMonthDayRectFs.get(0).left, mCurrentMonthDayRectFs.get(0).top,
                mCurrentMonthDayRectFs.get(CalendarUtils.NUM_CELL - 1).right, mCurrentMonthDayRectFs.get(CalendarUtils.NUM_CELL - 1).bottom);

        //当前月份DayBox初始化
        computeMonthDayBoxes(mCurrentYear, mCurrentMonth, Type.NONE);
    }

    private void computeMonthDayBoxes(int year, int month, Type updateType) {
        int currentYear = year;
        int currentMonth = month;
        switch (updateType) {
            case LAST:
                if (month == 0){
                    currentYear = year - 1;
                    currentMonth = 11;
                }
                break;
            case NEXT:
                if (month == 11) {
                    currentYear = year + 1;
                    currentMonth = 0;
                }
                break;
        }
        mCurrentMonthDayBoxes.clear();
        mCurrentMonthDayBoxes.addAll(mCalendarUtils.getMonthDate(currentYear, currentMonth));
        mCurrentYear = currentYear;
        mCurrentMonth = currentMonth;
        mTitleText = mCurrentYear + "年" + (mCurrentMonth + 1) + "月";

        int nextYear = (currentMonth == 11) ? currentYear + 1 : currentYear;
        int nextMonth = (currentMonth == 11) ? 0 : currentMonth + 1;
        mNextMonthDayBoxes.clear();
        mNextMonthDayBoxes.addAll(mCalendarUtils.getMonthDate(nextYear, nextMonth));

        int lastYear = (currentMonth == 0) ? currentYear - 1 : currentYear;
        int lastMonth = (currentMonth == 0) ? 11 : currentMonth - 1;
        mLastMonthDayBoxes.clear();
        mLastMonthDayBoxes.addAll(mCalendarUtils.getMonthDate(lastYear, lastMonth));

        for (int index = 0; index < CalendarUtils.NUM_CELL; index++) {
            mCurrentMonthDayBoxes.get(index).setRectF(mCurrentMonthDayRectFs.get(index));
            mNextMonthDayBoxes.get(index).setRectF(mNextMonthDayRectFs.get(index));
            mLastMonthDayBoxes.get(index).setRectF(mLastMonthDayRectFs.get(index));
        }
        Log.d(TAG, "updateMonth: mTitleText = " + mTitleText);
    }

    /**
     * 背景
     */
    private void drawBackground(Canvas canvas) {
        mbgPaint.setColor(Color.TRANSPARENT);
        canvas.drawRect(mViewRectF, mbgPaint);
    }

    /**
     * 年月
     */
    private void drawTitle(Canvas canvas) {
        mbgPaint.setColor(mTitleBg);
        canvas.drawRect(mTitleRectF, mbgPaint);

        mTextPaint.setColor(mTitleTextColor);
        mTextPaint.setTextSize(mTitleTextSize);
        Log.d(TAG, "titleText Coordinate: " + mTitleTextRectF.left + " " + mTitleTextRectF.top + " " + mTitleTextRectF.right + " " + mTitleTextRectF.bottom);
        canvas.drawText(mTitleText, mTitleTextRectF.centerX(), mTitleSpace + getFontLeading(mTextPaint), mTextPaint);
    }

    /**
     * 切换箭头
     */
    private void drawTitleArrow(Canvas canvas) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mTitleArrowLeft);
        int arrowHeight = bitmap.getHeight();
        int arrowWidth = bitmap.getWidth();
        //float left, float top
        int arrowLStart = (int) mTitleTextRectF.left - mTitleArrowSpace - arrowWidth;
        canvas.drawBitmap(bitmap, arrowLStart , (mTitleHeight - arrowHeight) / 2f, new Paint());
        bitmap = BitmapFactory.decodeResource(getResources(), mTitleArrowRight);
        int arrowRStart = (int) mTitleTextRectF.right + mTitleArrowSpace;
        canvas.drawBitmap(bitmap, arrowRStart, (mTitleHeight - arrowHeight) / 2f, new Paint());
    }

    /**
     * 星期
     */
    private void drawWeek(Canvas canvas) {
        String[] WEEK_STR = new String[]{"Sun", "Mon", "Tues", "Wed", "Thur", "Fri", "Sat"};
        mTextPaint.setColor(mWeekTextColor);
        for (int index = 0; index < CalendarUtils.NUM_COL; index++) {
            RectF rectF = mWeekRectFs.get(index);

            mbgPaint.setColor(mWeekBg);
            canvas.drawRect(rectF, mStrokePaint);

            mStrokePaint.setColor(Color.RED);
            canvas.drawRect(rectF, mStrokePaint);

            mTextPaint.setTextSize(mWeekTextSize);
            canvas.drawText(WEEK_STR[index], rectF.centerX(),
                    mTitleHeight + getFontLeading(mTextPaint), mTextPaint);
        }
    }

    /**
     * 每天
     */
    private void drawAllDay(Canvas canvas) {
        canvas.save();
        canvas.clipRect(0, mTitleHeight, mViewRectF.width(), mViewRectF.height());
        canvas.translate(mOffsetX, mOffsetY);
        for (int index = 0; index < CalendarUtils.NUM_CELL; index++) {
            drawDay(canvas, mCurrentMonthDayBoxes.get(index));
            drawDay(canvas, mNextMonthDayBoxes.get(index));
            drawDay(canvas, mLastMonthDayBoxes.get(index));
        }
        canvas.restore();
    }

    private void drawDay(Canvas canvas, DayBox dayBox) {
        mbgPaint.setColor(mDayBg);
        canvas.drawRect(dayBox.getRectF(), mbgPaint);

        mStrokePaint.setColor(Color.GREEN);
        canvas.drawRect(dayBox.getRectF(), mStrokePaint);

        mTextPaint.setColor(dayBox.isCurrentMonth() ? mDayTextColor : Color.GRAY);
        mTextPaint.setTextSize(mDayTextSize);
        //画当前日期
        if (dayBox.isCurrentMonth() && dayBox.getDay() == mCurrentDay) {
            mStrokePaint.setColor(Color.DKGRAY);
            PathEffect effect = new DashPathEffect(mCurrentDayDashPath, 1);
            mStrokePaint.setPathEffect(effect);   //设置画笔曲线间隔
            //绘制空心圆背景
            canvas.drawCircle(dayBox.getRectF().centerX(),
                    dayBox.getRectF().top + mDaySpace + (getFontLeading(mTextPaint) / 2f),
                    mSelectedRadius - mStrokeWidth, mStrokePaint);
            mStrokePaint.setPathEffect(null);
        }
        //画选中日期
        if (dayBox.getMonth() == mSelectedMonth && dayBox.getDay() == mSelectedDay) {
            mTextPaint.setColor(mSelectedDayColor);
            mbgPaint.setColor(Color.YELLOW);
            //绘制橙色圆背景，参数一是中心点的x轴，参数二是中心点的y轴，参数三是半径，参数四是paint对象；
            canvas.drawCircle(dayBox.getRectF().centerX(),
                    dayBox.getRectF().top + mDaySpace + (getFontLeading(mTextPaint) / 2f),
                    mSelectedRadius, mbgPaint);
        }
        canvas.drawText(String.valueOf(dayBox.getDay() + 1),
                dayBox.getRectF().centerX(),
                dayBox.getRectF().top + mDaySpace + getFontLeading(mTextPaint), mTextPaint);
    }

    /**
     * 外围线
     */
    private void drawOuterLine(Canvas canvas) {
        mStrokePaint.setColor(Color.BLUE);
        canvas.drawRect(mViewRectF, mStrokePaint);
    }

    /**
     * @return 返回指定笔和指定字符串的长度
     */
    public static float getFontlength(Paint paint, String str) {
        return paint.measureText(str);
    }

    /**
     * @return 返回指定笔的文字高度
     */
    public static float getFontHeight(Paint paint)  {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }
    /**
     * @return 返回指定笔离文字顶部的基准距离
     */
    public static float getFontLeading(Paint paint)  {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.leading- fm.ascent;
    }


}
