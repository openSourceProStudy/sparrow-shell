<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>hello sparrow mvc</title>
</head>
<body>

<h1>后端代码示例</h1>

<div>
    <pre>
    public ViewWithModel hello() throws BusinessException {
    //hello 默认映射到/template/hello.jsp
    return ViewWithModel.forward("hello", new HelloVO("我来自遥远的sparrow 星球,累死我了..."));
    }
        </pre>
</div>
<h1>前端获取参数</h1>
<div>
    <pre>
hello 为HelloVO的类名,自动将POJO 结尾字符（DTO,VO,BO）去除<br/>
hi \${hello.hello}
        </pre>
</div>
<h1>
    展示结果

</h1>
hi ${hello.hello}
</body>
</html>
