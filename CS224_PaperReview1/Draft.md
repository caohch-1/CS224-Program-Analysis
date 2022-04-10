# Slide 1
这篇paper发表在 ESEC/FSE 21‘ 上

# Slide 2
code-to-code search是指用一段代码去搜索一个库内和它近似的代码。

这篇paper关注的是cross-language的代码搜索，cross-language带来的挑战是不同语言之间存在语法和语义上的区别，故而需要找到一种能兼容多种语言的相似度比较方法。


# Slide 3
作者提出了他们的工具COSAL，他结合了两种静态分析技术和一种动态分析技术，并将三种技术获得的结果通过non-dominated sorting的方法进行结合，我们接下来分别介绍下三种技术。

# Slide 4
首先是token-based search，思路其实很简单，我们将代码中language-specific的keyword，比如public，static，def，assert之类去掉，把一些编程语言中的常见词去掉，比如python中的self之类，去掉英语中的一些常见stopwords，does，from之类的。

然后对于剩下的词进行分割，比如python中一般推荐变量命名用蛇形命名法，那么就根据下划线进行分割

最后去掉一些过短的token并且把所有token变小写

# Slide 5
然后是ast-based search，作者将不同语言的ast树映射到使用相同规则的ast上。具体来说就是这边的五条规则，其实就是把语言特有的一些语法、操作符或者控制结构等等都进行泛化，这样虽然会丢失很多的信息但是保证了各个语言拥有统一规则的ast树从而方便进行比较。

# Slide 6
最后是IO-based search，这是一种动态分析技术，这里作者并没有什么自己的设计或者创新，只是单纯将slacc这个工具拿来使用，这个工具是在ICSE20上的一篇paper里提出的。这一分析能够帮助作者的工具对于 那些内部结构、逻辑不太相同但是具有类似功能的代码块 也有比较好的效果。

# Slide 7
以往的工作往往会采用一个线性或者非线性的函数对各个分析的结果进行加权总和得到最终结果，但是这篇paper引入了non-dominated ranking，用一个例子解释下什么事non-dominated ranking

# Slide 8
接下来是实验部分，作者主要通过三个指标来评估他们方法的性能，准确率，成功率和平均排名的倒数，这三个都是越大越好也是常见的对搜索算法进行评估的机制

# Slide 9
第一个research question就是COSAL使用了三种分析技术，那超越了分别单独使用这些技术多少

# Slide 10
第二个rq是与现在实际产业中使用的code-to-code search的方法进行比较

# Slide 11
第三个rq与现有的sota的三个工具进行比较，他们有基于静态分析的，基于机器学习的和基于动态分析的

# Slide 12
第四个rq是针对code-to-code search的一个应用场景， code clone detection上进行了实验