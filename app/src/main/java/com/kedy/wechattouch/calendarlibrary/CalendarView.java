package com.kedy.wechattouch.calendarlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;

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
    /** 任务图标的大小*/
    private int mIconSize;
    /** 选中的日期的颜色*/
    private int mSelectedYear;
    private int mSelectedMonth;
    private int mSelectedDay;
    private int mSelectedDayColor;
    /** 选中背景*/
    private int mSelectedDayBg, mCurrentDayBg;
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

    private int mCurrentMonth;
    private int mCurrentYear;

    private Paint mStrokePaint;
    private Paint mbgPaint;
    private Paint mTextPaint;

    private int mTouchSlop;
    private float mTouchDownX;
    private float mTouchDownY;
    private float mOffsetX;
    private Type mScrollToType;

    private Scroller mContentScroller;
    private VelocityTracker mVelocityTracker;
    private boolean mIsDrawing;
    private boolean mIsClick;
    private boolean mIsScrolling;

    private Context mContext;
    private OnDateChangeListener mOnClickDayListener;

    enum Type {
        /**
         * 上个月，下个月，当前月
         */
        LAST,
        NEXT,
        NONE
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

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mContentScroller.computeScrollOffset()) {
            mOffsetX = mContentScroller.getCurrX();
            ViewCompat.postInvalidateOnAnimation(this);
        }else if (mIsScrolling) {
            mIsScrolling = false;
            mOffsetX = 0;
            Log.d(TAG, "compute scroll: mScrollType = " + mScrollToType);
            computeMonthDayBoxes(mCurrentYear, mCurrentMonth, mScrollToType);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsScrolling || mIsDrawing) {
            Log.d(TAG, "onTouchEvent: cannot touch");
            return false;
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMove(event);
                break;
            case MotionEvent.ACTION_UP:
                handleUp(event);
                break;
            default:
                Log.d(TAG, "onTouchEvent: unknown event Action = " + event.getAction());
        }
        return true;
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
        mSelectedYear = date[0];
        mSelectedMonth = date[1];
        mSelectedDay = date[2];
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
        mSelectedDayBg =  a.getColor(R.styleable.CalendarView_mSelectedDayBg, getResources().getColor(R.color.colorSelectedDay));
        mIconSize = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mIconSize, (float) mMaxWidthDp / 21));
        mCurrentDayBg = a.getColor(R.styleable.CalendarView_mCurrentDayBg, Color.YELLOW);
        mStrokeWidth = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mStrokeWidth, 1));

        mTitleSpace = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mTitleSpace, 5));
        mDaySpace = mCalendarUtils.dip2px(a.getDimension(R.styleable.CalendarView_mDaySpace, 2));
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
        //每行高度 = 行间距 + 日期字体高度 + 字间距 + 图标高度
        mRowHeight = mDaySpace + mDayHeight + mTextSpace + mIconSize;
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
        mNextRectF.set(mTitleTextRectF.right + mTitleArrowSpace, 0, mTitleTextRectF.right + mTitleArrowSpace, mTitleHeight);
        mLastRectF.set(mTitleTextRectF.left - mTitleArrowSpace, 0, mTitleTextRectF.left - mTitleArrowSpace, mTitleHeight);
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

    /***
     * 根据年月和即将发生的动作计算出完成动作后的新的当前年月
     *
     * @param year 当前年份
     * @param month 当前月份
     * @param updateType 向左滑或者向右滑
     */
    private void computeMonthDayBoxes(int year, int month, Type updateType) {
        int currentYear = year;
        int currentMonth = month;
        switch (updateType) {
            case LAST:
                if (month == 0){
                    currentYear = year - 1;
                    currentMonth = 11;
                }else {
                    currentMonth -= 1;
                }
                break;
            case NEXT:
                if (month == 11) {
                    currentYear = year + 1;
                    currentMonth = 0;
                }else {
                    currentMonth += 1;
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
            mCurrentMonthDayBoxes.get(index).setIconRectFs(mIconSize, mDayHeight, mDaySpace, mTextSpace);
            mNextMonthDayBoxes.get(index).setRectF(mNextMonthDayRectFs.get(index));
            mNextMonthDayBoxes.get(index).setIconRectFs(mIconSize, mDayHeight, mDaySpace, mTextSpace);
            mLastMonthDayBoxes.get(index).setRectF(mLastMonthDayRectFs.get(index));
            mLastMonthDayBoxes.get(index).setIconRectFs(mIconSize, mDayHeight, mDaySpace, mTextSpace);
        }
        Log.d(TAG, "updateMonth: mTitleText = " + mTitleText);
    }

    /**
     * 背景
     */
    private void drawBackground(Canvas canvas) {
//        mbgPaint.setColor(Color.TRANSPARENT);
//        canvas.drawRect(mViewRectF, mbgPaint);
//        canvas.setBitmap();
        setBackgroundResource(R.drawable.calendar_background);
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
        mLastRectF.left = mLastRectF.right - arrowWidth;
        mNextRectF.right = mNextRectF.left + arrowWidth;
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
        String[] WEEK_STR = new String[]{"日", "一", "二", "三", "四", "五", "六"};
        mTextPaint.setColor(mWeekTextColor);
        for (int index = 0; index < CalendarUtils.NUM_COL; index++) {
            RectF rectF = mWeekRectFs.get(index);

            mbgPaint.setColor(mWeekBg);
            canvas.drawRect(rectF, mbgPaint);

//            mStrokePaint.setColor(Color.RED);
//            canvas.drawRect(rectF, mStrokePaint);

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
        canvas.translate(mOffsetX, 0);
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

        mStrokePaint.setColor(Color.LTGRAY);
        canvas.drawRect(dayBox.getRectF(), mStrokePaint);

        mTextPaint.setColor(dayBox.isCurrentMonth() ? mDayTextColor : Color.GRAY);
        mTextPaint.setTextSize(mDayTextSize);
        //画当前日期
        if (mCalendarUtils.isToday(dayBox.getDate())) {
            mbgPaint.setColor(mCurrentDayBg);
            mbgPaint.setAlpha(80);
            canvas.drawRect(dayBox.getRectF(), mbgPaint);
            mbgPaint.setAlpha(100);


            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_birthday);
            List<RectF> rectF = dayBox.getDoubleIconRectFs();
            canvas.drawBitmap(bitmap, null, rectF.get(0), new Paint());
            canvas.drawBitmap(bitmap, null, rectF.get(1), new Paint());

        }
        //画选中日期
        else if (dayBox.getYear() == mSelectedYear && dayBox.getMonth() == mSelectedMonth && dayBox.getDay() == mSelectedDay) {
            mTextPaint.setColor(mSelectedDayColor);
            mbgPaint.setColor(mSelectedDayBg);
            mbgPaint.setAlpha(80);
            canvas.drawRect(dayBox.getRectF(), mbgPaint);
            mbgPaint.setAlpha(100);


            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_shopping);
            List<RectF> rectF = dayBox.getTripleIconRectFs();
            canvas.drawBitmap(bitmap, null, rectF.get(0), new Paint());
            canvas.drawBitmap(bitmap, null, rectF.get(1), new Paint());
            canvas.drawBitmap(bitmap, null, rectF.get(2), new Paint());
        }
        canvas.drawText(String.valueOf(dayBox.getDay() + 1),
                dayBox.getRectF().centerX(),
                dayBox.getRectF().top + mDaySpace + getFontLeading(mTextPaint), mTextPaint);
    }

    /**
     * 外围线
     */
    private void drawOuterLine(Canvas canvas) {
//        mStrokePaint.setColor(Color.BLUE);
//        canvas.drawRect(mViewRectF, mStrokePaint);
    }

    private void handleDown(MotionEvent event) {
        mTouchDownX = event.getX();
        mTouchDownY = event.getY();
        mIsClick = true;
    }

    private void handleMove(MotionEvent event) {
        mOffsetX = event.getX() - mTouchDownX;

        //设置最大单次滑动距离
        if (Math.abs(mOffsetX) >= mViewRectF.width()) {
            mOffsetX = mOffsetX > 0 ? mViewRectF.width() : -mViewRectF.width();
        }

        //判断是否为滑动
        if (Math.abs(mOffsetX) > mTouchSlop) {
            mIsClick = false;
        }

        if (mIsScrolling || !mDayContentRectF.contains(mTouchDownX, mTouchDownY)) {
            return;
        }

        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void handleUp(MotionEvent event) {
        if (mIsClick) {
            mOffsetX = 0;
            if (mNextRectF.contains(mTouchDownX, mTouchDownY)) {
                startScrollOnClick(Type.NEXT);
            } else if (mLastRectF.contains(mTouchDownX, mTouchDownY)) {
                startScrollOnClick(Type.LAST);
            } else if (mDayContentRectF.contains(mTouchDownX, mTouchDownY)) {
                if (mOnClickDayListener != null) {
                    for (DayBox dayBox : mCurrentMonthDayBoxes) {
                        if (dayBox.isCurrentMonth() && dayBox.isContains(mTouchDownX, mTouchDownY)) {
                            mSelectedYear = dayBox.getYear();
                            mSelectedMonth = dayBox.getMonth();
                            mSelectedDay = dayBox.getDay();
                            mOnClickDayListener.onClickDay(mSelectedYear, mSelectedMonth, mSelectedDay);
                            ViewCompat.postInvalidateOnAnimation(this);
                            break;
                        }
                    }
                }
            }
        } else if (mDayContentRectF.contains(mTouchDownX, mTouchDownY)) {
            startScroll(event);
        }
        mIsClick = false;
    }

    private void startScrollOnClick(Type type) {
        mScrollToType = type;
        int dx = (int) mViewRectF.width();
        if (type == Type.NEXT) {
            dx = -dx;
        }
        mIsScrolling = true;
        mContentScroller.startScroll(0, 0, dx, 0, 500);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void startScroll(MotionEvent event) {
        mIsScrolling = true;
        mVelocityTracker.computeCurrentVelocity(1000);
        int dx = (int) -mOffsetX;
        float viewWidth = mViewRectF.width();
        mScrollToType = Type.NONE;
        if (mVelocityTracker.getXVelocity() < -200 || mOffsetX < -viewWidth * 0.3) {
            dx = (int) -(viewWidth + mOffsetX);
            mScrollToType = Type.NEXT;
        } else if (mVelocityTracker.getXVelocity() > 200 || mOffsetX > viewWidth * 0.3) {
            dx = (int) (viewWidth - mOffsetX);
            mScrollToType = Type.LAST;
        }
        mContentScroller.startScroll((int) mOffsetX, 0, dx, 0, 500);
        ViewCompat.postInvalidateOnAnimation(this);
        mVelocityTracker.clear();
    }

    public interface OnDateChangeListener {
        void onClickDay(int year, int month, int day);
    }

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        mOnClickDayListener = listener;
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
