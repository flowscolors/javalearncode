## class文件
class文件，所有平台统一支持的文件存储格式。  class文件结构是以8字节为基础单位的二进制流。  


## jar包资源
jar 包是一个单独的文件而非文件夹，所以绝对不可能通过 “file:/e:/…/classes/application.properties” 这种形式的文件 URL 来定位 application.properties。即使是相对路径，也无法定位到 jar 文件内的 application.properties 文件。

对于java程序而言，，所有的 .class 文件都在 jar 中，既然 ClassLoader 能正确的加载 .class 文件，那么肯定也可以加载 jar 中的其他文件。所以可以用classload去加载文件，但是这里实际已经不是文件的概念，而是资源的概念。

## 如何替换jar包中代码
1.下载 jar 包源码。如果无法获得源码，需要用反编译工具反编译。  
2.找到 jar 中你想要修改的类，在自己的工程目录下，创建一个同该类一样的包(package)。  
3.把你要修改的类复制到该包中。此时，可以对该类进行修改。  
4.ebug 项目，在该类中打上断点，可以看到代码执行时会进入这个新的类中，说明走的是改后的代码。  
5.注意，IDEA 会出现 alternative source available for the class 提示。这个是可以随时切换想要执行的同名类。执行了就切换了。  

到这里，调试阶段修改 jar 包中源码的方式就到此为止了。但如果项目想要使用修改后的类打包到生产上运行，还需要继续进行下面的操作。

1.接着上面的第 4 步，项目可以正常打包上线。  
2.如果不想在工程中显示创建一个同名类，想要直接修改 jar 包中的代码的话，可以对新修改的同名类进行编译生成 .class 文件。  
3.然后将 jar 包解压，找到对应类的 .class 文件，用刚刚编译的新的 .class 文件替换。然后将解压后的 jar 包打成 zip 包。  
4.最后，将 zip 包后缀修改为 .jar，替换原有工程的 jar 包即可。  
以上就是修改 jar 包中源码的方法。

但是后者的方法可能会出现问题，tomcat运行jar包或者war包，读取文件，实际有一步解压的工作，unzipEntry，获取到class文件。  
当jar包、war包有问题的时候，该步骤会尤其消耗CPU，导致程序性能下降，甚至出现报错。   

参考文档：  
https://blog.csdn.net/antony1776/article/details/89249509
https://taaang.github.io/blog/jvm/2018/04/02/jvm-crash-when-jar-modify/