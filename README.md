# NestedScrolling机制学习
来张图镇个楼

背景
有关嵌套滑动的类大多直接继承现有的ViewGroup。例如StickyNavLayoutCopy.java。直接处理dispatchTouchEvent，onInterceptTouchEvent，onTouchEvent方法，当父视图拦截了本事件序列当前event后，若要想把事件传递给子视图处理下后面事件的话就是不可能的了，有以下方法可以继续交给子View处理此事件了：
1.在onTouchEvent()中找到相应的子view分发给其事件
2.在dispatchTouchEvent中直接分发给相应的子view
3.在onTouchEvent()中模拟事件（MotionEvent.obtain(MotionEvent other)）分发给子View

StickyNavLayoutCopy.java作为父view实现方式：
此视图拦截事件，然后模拟事件分发给子view：当把粘性头部滑过以后会模拟一个down事件传给子view，直到此事件序列结束

存在问题
当刚开始滑动时此事件消费此事件后，直到事件序列处理到一半的时候，此时如果住下发送事件，就会模拟一个down事件，子视图收到此事件后，然后会在up事件时子视图就会同时收到一个up事件，这时相当于子视图收到一个down/up事件组，也就成功的模拟了一次onClick点击事件，但真实情况我们是不想让此子视图响应此点击事件的。所以只能另加一布尔变量 a 来处理此种情况。当由此视图模拟down事件给子视图时，将 a 置位，当在up事件时，此父视图不再向下分发up事件，防止子视图形成一个onClick事件。



当前解决方式弊端
人非圣贤谁无过啊！

模拟事件会遗漏某些事件
三种方法中有很多状态要改变和判断，一些通用的逻辑与业务掺杂太多
代码丑，只能自己维护
即使有注释了，因为2原因，还是不容易读懂
解决方式：
NestScrolling滑动机制（学习呀！）

知识点温习：
事件分发：
1、public boolean dispatchTouchEvent(MotionEvent event)     事件分发的起点
Pass the touch screen motion event down to the target view, or this view if it is the target.

将此触摸事件传递至目标view，如果当前视图是目标view的话就传给它本身。

@param event The motion event to be dispatched.

参数event：当前要分发的事件

@return True if the event was handled by the view, false otherwise.

返回值：true表示当前视图消费了此事件，false不消费



2、public boolean onInterceptTouchEvent(MotionEvent ev)      ViewGroup的方法
在dispatchTouchEvent方法内部调用，用来判断是否拦截某个事件，如果当前View拦截了某个事件，那么在同一个事件序列当中，此方法不会被再次调用，返回结果表示是否拦截当前事件。

@return Return true to steal motion events from the children and have them dispatched to this ViewGroup through onTouchEvent(). The current target will receive an ACTION_CANCEL event, and no further messages will be delivered here.

返回值：true表示不分发此事件给子view，拦截此事件交给自己的onTouchEvent()处理。而且之前的target视图会收到一个cancel的事件，并且消息不会再传到此方法。



3、public boolean onTouchEvent(MotionEvent event)
在dispatchTouchEvent方法中调用，用来处理点击事件，返回结果表示是否消耗当前事件，如果不消耗，则在同一个事件序列中，当前View无法再次接收到事件。

@return True if the event was handled, false otherwise.

返回值：true表示此事件被处理消耗，否则表示未消耗

此事件的优先级在OnTouchListener的onTouch方法以后

优先级顺序： OnTouchListener.onTouch()>View.onTouchEvent()>OnclickListener.onClick()



关于事件分发的结论：
某个ViewGroup一旦决定拦截，那么这一个事件序列都只能由它来处理（如果事件序列能够传递给它的话），并且它的onInterceptTouchEvent不会再被调用。
ViewGroup默认不拦截任何事件。Android源码中ViewGroup的onInterceptTouchEvent方法默认返回false。
View的onTouchEvent默认都会消耗事件（返回true），除非它是不可点击的clickable  和longClickable同时为false）。View的longClickable属性默认都为false，clickable属性要分情况，比如Button的clickable属性默认为true，而TextView的clickable属性默认为false。
事件传递过程是由外向内的，即事件总是先传递给父元素，然后再由父元素分发给子View，通过requestDisallowInterceptTouchEvent方法可以在子元素中干预父元素的事件分发过程，但是ACTION_DOWN事件除外。



NestScrolling机制


Android 5.0 Lollipop 之后，Google 官方通过 嵌套滑动机制 解决了传统 Android 事件分发无法共享事件这个问题。系统自带的View和ViewGroup都增加了 嵌套滑动机制 相关的方法了（但是默认不会被调用，因此默认不具备嵌套滑动功能）。因此为了兼容，google又提供了两个接口以及相应的帮助类来支持嵌套滑动：

NestedScrollingParent：  作为父控件，支持嵌套滑动功能。
NestedScrollingChild：  作为子控件，支持嵌套滑动功能。
NestedScrollingParentHelper：  作为父控件的常用操作的帮助类
NestedScrollingChildHelper：  作为父控件的常用操作的帮助类


基本原理
嵌套滑动机制的基本原理可以认为是事件共享，即当子控件接收到滑动事件，准备要滑动时，会先通知父控件(startNestedScroll）；然后在滑动之前，会先询问父控件是否要滑动（dispatchNestedPreScroll)；如果父控件响应该事件进行了滑动，那么就会通知子控件它具体消耗了多少滑动距离；然后交由子控件处理剩余的滑动距离；最后子控件滑动结束后，如果滑动距离还有剩余，就会再问一下父控件是否需要在继续滑动剩下的距离（dispatchNestedScroll)...



官司方说明
一课-教学研发 > 19.3.30-张飞-nestedScroll原理分享 > image2019-3-30_16-34-40.png    一课-教学研发 > 19.3.30-张飞-nestedScroll原理分享 > image2019-3-30_16-34-53.png



翻译一下各个接口：
NestedScrollingChild
startNestedScroll : 起始方法, 主要作用是找到接收滑动距离信息的外控件.
dispatchNestedPreScroll : 在内控件处理滑动前把滑动信息分发给外控件.
dispatchNestedScroll : 在内控件处理完滑动后把剩下的滑动距离信息分发给外控件.
stopNestedScroll : 结束方法, 主要作用就是清空嵌套滑动的相关状态
setNestedScrollingEnabled和isNestedScrollingEnabled : 一对get&set方法, 用来判断控件是否支持嵌套滑动.
dispatchNestedPreFling和dispatchNestedFling : 跟Scroll的对应方法作用类似, 不过分发的不是滑动信息而是Fling信息.(这个Fling暂时翻译为惯性滑动吧)

可以看出

内控件是嵌套滑动的发起者.

NestedScrollingParent
因为内控件是发起者, 所以外控件的大部分方法都是被内控件的对应方法回调的.
onStartNestedScroll : 对应startNestedScroll, 内控件通过调用外控件的这个方法来确定外控件是否接收滑动信息.
onNestedScrollAccepted : 当外控件确定接收滑动信息后该方法被回调, 可以让外控件针对嵌套滑动做一些前期工作.
onNestedPreScroll : 关键方法, 接收内控件处理滑动前的滑动距离信息, 在这里外控件可以优先响应滑动操作, 消耗部分或者全部滑动距离.
onNestedScroll : 关键方法, 接收内控件处理完滑动后的滑动距离信息, 在这里外控件可以选择是否处理剩余的滑动距离.
onStopNestedScroll : 对应stopNestedScroll, 用来做一些收尾工作.
getNestedScrollAxes : 返回嵌套滑动的方向, 区分横向滑动和竖向滑动, 作用不大
onNestedPreFling和onNestedFling : 同上略

外控件通过onNestedPreScroll和onNestedScroll来接收内控件响应滑动前后的滑动距离信息.

再次指出, 这两个方法是实现嵌套滑动效果的关键方法.



对应关系
NestedScrollingChild

NestedScrollingParent

startNestedScroll

startNestedScroll

onStartNestedScroll

前者的调用会触发后者的调用,然后后者的返回值将决定后续的嵌套滑动事件是否能传递给父View,如果返回false,父View将不处理嵌套滑动事件,一般前者的返回值即后者的返回值



onNestedScrollAccepted

如果onStartNestedScroll返回true,则回调此方法

stopNestedScroll

onStopNestedScroll



dispatchNestedScroll

onNestedScroll



dispatchNestedPreScroll

onNestedPreScroll



dispatchNestedFling

onNestedFling



dispatchNestedPreFling

onNestedPreFling





getNestedScrollAxes

获得滑动方向,没有回调,为主动调用的方法



时序图


源码分析
Api21以后的sdk里view和viewgroup的源码里会有相应方法，具体的代码可以直接看相关方法，同时也可参考NestedScrollView（既是Child又是Parent）和RecyclerView（Child）.



总结
子view需要在onTouchEvent()中分发move事件调用帮助类的dispatchNestedPreScroll询问父视图消耗是否消耗，然后消费父视图未消费的consumed[2]中的x/y，如果还有未消耗的再通过dispatchNestedScroll传给父视图
父视图则只需关心onNestedPreScroll和onNestedScroll来消费子视图通过传过来的consumed[2]中的x/y
同理，关于up事件以后的fling操作也同move操作一样，只是fling操作消费的是velecity(速度)。
事件消费是由子view发起的，startNestedScroll和stopNestedScroll是成对出现的，滑动前和滑动结束后调用，此时可以用来做一些滑动的初始化和收尾事件




参考文档：

一点见解: Android嵌套滑动和NestedScrollView  及 Android SDK 25 API



