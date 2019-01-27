##### 在Android原生APP开发中，文件下载是一个常用的模块，结合Retrofit2和Rxjava2封装了一个下载管理器。 

#### 1. 创建下载任务  

```java
DownloadTask task = new DownloadTask.Builder()
                .setUrl(url)
                .setLocalPath(path)
	            .setPriority(1)
                .setDonwloadListener(listener)
                .build();
```
在调用`build()`方法时会对参数进行检查，所以`build()`需放到最后调用且下载地址和本地文件路径不能为`null`。  

#### 2. 提交下载任务  

`DownloadManager`是下载队列的管理类，提供了一个下载任务的方法：  

```java
DownloadFuture download(@NonNull DownloadTask task)
```
为了方便使用，还提供了另一个方法：  

```java
DownloadFuture download(String url, String path, DownloadListener listener)
```

#### 3. 任务的暂停和取消  

任务的暂停和取消通过`DownloadFuture`来管理的：  

```java
// 暂停
void pause()
// 恢复
void resume()
// 取消
boolean cancel(boolean mayInterruptIfRunning)
```

#### 4. 效果如下  

![](https://i.imgur.com/QUujPND.gif)
