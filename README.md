# itechnika-example-session-clustering-tomcat
NginX + Tomcat TCP Session Clustering

***

### Blog  
***Korean***: https://d2j2logs.blogspot.com/2025/03/session-clustering-tomcat.html  
***English***: https://d2j2logs-en.blogspot.com/2025/03/session-clustering-tomcat.html  
***Basaha Indonesia***: https://d2j2logs-id.blogspot.com/2025/03/session-clustering-tomcat.html  

***

### Included Items
+ Tomcat and NginX Configuration Files for Session Clustering  
+ Java Servlet for Session Manipulation

### Tomcat Configuration
#### conf/server.xml
```xml
<Engine name="Catalina" defaultHost="localhost" jvmRoute="jvm1">

  ...

  <!-- Server 1 -->
  <Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster" channelSendOptions="8" channelStartOptions="3">
    <Manager className="org.apache.catalina.ha.session.DeltaManager" expireSessionsOnShutdown="false" notifyListenersOnReplication="true"/>
    <Channel className="org.apache.catalina.tribes.group.GroupChannel">
      <Sender className="org.apache.catalina.tribes.transport.ReplicationTransmitter">
        <Transport className="org.apache.catalina.tribes.transport.nio.PooledParallelSender"/>
      </Sender>
      <Receiver address="10.0.0.21" autoBind="0" className="org.apache.catalina.tribes.transport.nio.NioReceiver" maxThreads="6" port="4000" selectorTimeout="5000"/>
      <Interceptor className="org.apache.catalina.tribes.group.interceptors.TcpPingInterceptor" staticOnly="true"/>
      <Interceptor className="org.apache.catalina.tribes.group.interceptors.TcpFailureDetector"/>
      <Interceptor className="org.apache.catalina.tribes.group.interceptors.MessageDispatchInterceptor"/>
      <Interceptor className="org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor">
        <Member className="org.apache.catalina.tribes.membership.StaticMember" port="4000" host="10.0.0.22" uniqueId="{0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2}"/>
        <Member className="org.apache.catalina.tribes.membership.StaticMember" port="4000" host="10.0.0.23" uniqueId="{0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,3}"/>
      </Interceptor>
    </Channel>
    <Valve className="org.apache.catalina.ha.tcp.ReplicationValve" filter=""/>
    <ClusterListener className="org.apache.catalina.ha.session.ClusterSessionListener"/>
  </Cluster>

  ...

</Engine>
```

#### WEB-INF/web.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd" version="4.0">
  <distributable/>
</web-app>
```

### NginX Configuration
#### conf/nginx.conf
```nginx
upstream jvm {
    random;
    server 10.0.0.21:8080 weight=1 max_fails=10 fail_timeout=10s;
    server 10.0.0.22:8080 weight=1 max_fails=10 fail_timeout=10s;
    server 10.0.0.23:8080 weight=1 max_fails=10 fail_timeout=10s;
}

server {
    listen       80;
    server_name  localhost;

    location / {
        #root   html;
        #index  index.html index.htm;
        proxy_pass http://jvm;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header HOST $http_host;
        proxy_set_header X-NginX-Proxy true;
        proxy_set_header X-Session-ID $http_cookie;
        charset utf-8;
    }
    ...
  }
  ...
}
```

### Testing Servlet
#### Create Session
``` java
@WebServlet("/create.do")
public class CreateSessionServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String param = request.getParameter("param");

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(true);
        session.setAttribute("param", param);

        String sessionId = session.getId();
        out.println(String.format("<p>Session(%s) has been created.</p>", sessionId));
    }
}
```

#### Check Session
``` java
public class CheckSessionServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            String param = (String)session.getAttribute("param");
            out.println(String.format("<p>Session Id: %s<br/>Parameter: %s</p>.", sessionId, param));
        } else {
            out.println("<p>Session does not exist.</p>");
        }
    }
}
```

#### Destory Session
``` java
@WebServlet("/destroy.do")
public class DestroySessionServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            session.invalidate();
            out.println(String.format("<p>Session(%s) has been destroyed.</p>", sessionId));
        } else {
            out.println("<p>Session does not exist.</p>");
        }
    }
}
```
