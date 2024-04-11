package com.jsh.test.service;

import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ClassFindService {
    public static final String version = "ClassInfo v3.0";
    public static final boolean classAutoComplete = false;
    public static final int maxListCount = 10;
    public static final int minPackageDepth = 3;

    public static List resourceNameList = new ArrayList<>();
    public static List classpathList = new ArrayList<>();
    public static String jeus_home = System.getProperty("jeus.home");
    public static String fsep = System.getProperty("file.separator");
    public static String psep = System.getProperty("path.separator");
    public static int loadedResourceCount = 0;
    public static int percentComplete = 0;
    public static String libraryLoading = "Reading";
    public static boolean isLoading = false;
    String pname[] = {
            "JEUS",
            "Log4J",
            "OracleJDBCDriver",
            "JavaMail",
            "LDAP",
            "JDom",
            "Parser",
            "WebT",
    };

    String cname[] = {
            "/javax/servlet/http/HttpServlet.class",
            "/org/apache/log4j/BasicConfigurator.class",
            "/oracle/jdbc/driver/OracleDriver.class",
            "/com/sun/mail/pop3/Response.class",
            "/com/novell/ldap/LDAPConnection.class",
            "/org/jdom/input/DOMBuilder.class",
            "/javax/xml/parsers/SAXParser.class",
            "/tmax/webt/WebtSystem.class"
    };
    public boolean isClass = false;
    public boolean isDir = false;
    public java.net.URL resUrl = null;
    public File absFile = null;
    public boolean existFile = true;
    public boolean canRead = true;
    public boolean isLink = false;
    public boolean isErr = false;
    public Class cls = null;
    public ClassLoader cl = null;
    public String noPackageName = "";
    public String noExtName = "";
    private String findcp;
    private String action;
    private String resource;
    public void setClassFindService(String findcp, String action, String resource){
        this.findcp = findcp;
        this.action = action;
        this.resource = resource;
    }

    public ArrayList<String> getClassPath(){
        ArrayList<String> arrayList = new ArrayList<>();
        String findCP = this.findcp;
        String resName = this.resource;
        if(findCP == null || resName == null || findCP.length() < 1 || resName.length() < 1) {
            return null;
        }
        boolean inClasspath = findCP.trim().equals("true");
        resName = resName.trim();
        if(inClasspath) {
            resUrl = getResourceURL(resName);
            resUrl = (resUrl == null) ? getResourceURL(replace(resName, ".", "/") + ".class") : resUrl;
            resName = resName.endsWith("/") ? resName.substring(0, resName.length()-1) : resName;
            noPackageName = (resName.lastIndexOf("/")>-1) ? resName.substring(resName.lastIndexOf("/")+1) : resName;
        } else {
            resName = (fsep.equals("/") || (fsep.equals("\\") && resName.indexOf(":") > 0)) ? resName : ("C:/" + resName);
            resName = (fsep.equals("/")) ? replace(resName, "\\", fsep) : replace(resName, "/", fsep);
            resName = replace(resName, fsep+fsep, fsep);
            absFile = new File(resName);
            try {
                existFile = absFile.exists();
                canRead = absFile.canRead();
                resName = (resName.length()>1 && resName.endsWith(fsep)) ? resName.substring(0, resName.length()-1) : resName;
                noPackageName = (resName.lastIndexOf(fsep)>-1) ? resName.substring(resName.lastIndexOf(fsep)+1) : resName;
                isLink = !(absFile.getAbsolutePath().equals(absFile.getCanonicalPath()));
            } catch(SecurityException e) {
                canRead = false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(inClasspath) {
            if(resUrl == null) {
                if(existResourceName(resName)) {
                    isClass = false;
                    isDir = true;
                } else {
                    isErr = true;
                }
            } else {
                int fidx = resUrl.getFile().indexOf("!/");
                isClass = resUrl.getFile().endsWith(".class");
                isDir = ((fidx > -1 && resUrl.getFile().substring(fidx+2).indexOf(".") < 0) || (fidx < 0 && new File(checkLocation(resUrl.getFile())).isDirectory())) ? true : false;
                if(!isDir && isClass) {
                    if(resName.startsWith("/"))
                        resName = resName.substring(1);
                    resName = resName.replace('/', '.');
                    noExtName = (resName.endsWith(".class")) ? removeExt(resName) : resName;
                    try {
                        cls = Class.forName(removeExt(resName));
                        cl = cls.getClassLoader();
                    } catch(NoClassDefFoundError e1) {
                        e1.printStackTrace();
                        isErr = true;
                    } catch(ClassNotFoundException e2) {
                        e2.printStackTrace();
                        isErr = true;
                    }
                }
            }
        } else {
            if(!existFile) {
                isErr = true;
            } else if(!canRead) {
                isErr = true;
            } else {
                isDir = absFile.isDirectory();
            }
        }
        String showName = (isDir) ? noPackageName : ((inClasspath && isClass)?noExtName:noPackageName);
        showName = (showName.trim().equals("")) ? "/" : showName;
        if(inClasspath && isDir && minPackageDepth > 0 && minPackageDepth > getPackageDepth(resName)) {
            isErr = true;
        }
        if(cls == null){
            for(int i=0; i<6; i++){
                arrayList.add("존재하지 않음");
            }
            return arrayList;
        }
        String result1 = (inClasspath && isClass)?getModifierString(cls):"";
        result1 = result1 + " " + showName;
        String result2 = (!inClasspath)?absFile.getAbsolutePath():((!isDir)?checkLocation(resUrl.getFile()):"N/A");
        String result3 = (cl==null) ? "&nbsp;" : cl.toString();
        String result4 = getModifierString(cls.getSuperclass()) + " ";
        if(cls.getSuperclass() == null){
            result4 = result4 + "Null";
        }else{
            result4 = result4 + cls.getSuperclass().getName();
        }
        String result5 = cls.isInterface() ? "Yes" : "No";
        String result6 = cls.isPrimitive() ? "Yes" : "No";
        arrayList.add(result1);
        arrayList.add(result2);
        arrayList.add(result3);
        arrayList.add(result4);
        arrayList.add(result5);
        arrayList.add(result6);



        return arrayList;
    }

    public synchronized void getLoadedResources(ServletContext application, String cl) {
        String contextLoader = System.getProperty("dal.classinfo.contextloader");
        if(!classAutoComplete || (resourceNameList.size()>0 && cl.equals(contextLoader)))
            return;
        System.setProperty("dal.classinfo.contextloader", cl);
        resourceNameList = new ArrayList();
        String sun_path = System.getProperty("sun.boot.class.path");
        String java_path = System.getProperty("java.class.path");
        addClasspathList(sun_path, "");
        addClasspathList(java_path, "");
        String jeus_server_path = null;
        String jeus_prepend_path = null;
        String jeus_app_path = null;
        String jeus_ds_path = null;
        String jeus_sys_path = null;
        String jeus_lib = null;
        if(jeus_home != null && jeus_home.length() > 0) {
            jeus_server_path = System.getProperty("jeus.server.classpath");
            jeus_prepend_path = System.getProperty("jeus.prepend.classpath");
            jeus_lib = jeus_home + fsep + "lib" + fsep;
            jeus_sys_path = jeus_lib + "system";
            jeus_app_path = jeus_lib + "application";
            jeus_ds_path = jeus_lib + "datasource";
            addClasspathList(jeus_server_path, jeus_sys_path);
            addClasspathList(jeus_prepend_path, jeus_sys_path);
            addClasspathList(getAllFileList(jeus_sys_path), jeus_sys_path);
            addClasspathList(getAllFileList(jeus_app_path), jeus_app_path);
            addClasspathList(getAllFileList(jeus_ds_path), jeus_ds_path);
            addClasspathList(jeus_app_path, "");
        }
        String app_webinf = application.getRealPath("/") + "WEB-INF" + fsep;
        String app_lib_path = app_webinf + "lib";
        String app_class_path = app_webinf + "classes";
        addClasspathList(getAllFileList(app_lib_path), app_lib_path);
        addClasspathList(app_class_path, "");
        try {
            getResourceNameList();
        } catch(Exception e) {
            loadedResourceCount = -1;
            percentComplete = -1;
            libraryLoading = null;
            e.printStackTrace();
        }
    }

    public String getAllFileList(String path) {
        StringBuffer ret = new StringBuffer("");
        try {
            File f_path = new File(path);
            File files[] = f_path.listFiles();
            if(files == null)
                return "";
            for(int i = 0; i < files.length; i++) {
                try {
                    if(files[i].isFile()) {
                        String f = files[i].getName();
                        if(f.toLowerCase().endsWith(".jar") || f.toLowerCase().endsWith(".zip"))
                            ret.append(f + psep);
                    }
                } catch(SecurityException se) {}
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            return ret.toString();
        }
    }

    public void addClasspathList(String pathlist, String default_path) {
        if(pathlist == null || pathlist.length() < 1)
            return;
        StringTokenizer st = new StringTokenizer(pathlist, psep);
        String path;
        while(st.hasMoreTokens()) {
            path = st.nextToken();
            if(path.indexOf(fsep) < 0)
                path = default_path + fsep + path.trim();
            if(!existElement(path, resourceNameList))
                classpathList.add(path);
        }
    }

    public void getResourceNameList() throws Exception {
        Iterator iter = classpathList.iterator();
        String path;
        int cnt = 0;
        int tot = classpathList.size();
        System.out.println("[ClassInfo] ********************** Loaded Classpath List **********************");
        while(iter.hasNext()) {
            path = (String)iter.next();
            System.out.println("[ClassInfo] " + path);
            setProgressVars((new File(path)).getName(), cnt, tot);
            getResourceNames(path);
            cnt++;
        }
        System.out.println("[ClassInfo] *******************************************************************");
        setProgressVars("Complete", tot, tot);
    }

    public void setProgressVars(String libname, int complete, int total) {
        BigDecimal bd = new BigDecimal((double)complete / (double)total * 100.0);
        percentComplete = bd.setScale(0, BigDecimal.ROUND_CEILING).intValue();
        loadedResourceCount = resourceNameList.size();
        libraryLoading = libname;
    }

    public void getResourceNames(String path) {
        if(path.toLowerCase().endsWith(".jar") || path.toLowerCase().endsWith(".zip")) {
            getResourceNamesFromFile(path);
        } else {
            String jeus_app_path = (jeus_home!=null && jeus_home.length()>0) ? (jeus_home + fsep + "lib" + fsep + "application") : null;
            getResourceNamesFromDir(path, null, (jeus_app_path!=null && path.equals(jeus_app_path))?true:false);
        }
    }

    public void getResourceNamesFromFile(String path) {
        try {
            File f_path = new File(path);
            if(f_path.exists() == false)
                return;
            ZipFile zf = new ZipFile(f_path);
            Enumeration e = zf.entries();
            ZipEntry entry;
            String cl;
            String ext;
            boolean isClass;
            while(e.hasMoreElements()) {
                entry = (ZipEntry)e.nextElement();
                cl = entry.getName();
                if(!cl.endsWith("/")) {
                    ext = getExt(cl);
                    isClass = ext.equalsIgnoreCase(".class");
                    cl = removeSuffix(cl, ext);
                    cl = replace(cl, ".", "/") + ((isClass)?"":ext);
                    if(existElement(cl, resourceNameList) == false)
                        resourceNameList.add(cl);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void getResourceNamesFromDir(String path, String root, boolean readLib) {
        if(root == null)
            root = path;
        try {
            File f_path = new File(path);
            if(f_path.exists() == false)
                return;
            File files[] = f_path.listFiles();
            if(files == null)
                return;
            String fname;
            String ext;
            for(int i = 0; i < files.length; i++) {
                fname = files[i].getName();
                ext = getExt(fname);
                try {
                    if(files[i].isFile()) {
                        if(readLib && (ext.equalsIgnoreCase(".jar") || ext.equalsIgnoreCase(".zip"))) {
                            getResourceNamesFromFile(path + fsep + fname);
                        } else {
                            if(ext.equalsIgnoreCase(".class"))
                                fname = removeExt(fname);
                            if(path.length() != root.length()) {
                                fname = replace(path.substring(root.length()+1), fsep, "/") + "/" + fname;
                            }
                            if(existElement(fname, resourceNameList) == false)
                                resourceNameList.add(fname);
                        }
                    } else if(files[i].isDirectory()) {
                        getResourceNamesFromDir(files[i].getAbsolutePath(), root, readLib);
                    }
                } catch(SecurityException se) {}
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String removeSuffix(String s, String suffix) {
        if(s == null)
            return s;
        if(s.length() < suffix.length() || s.toLowerCase().endsWith(suffix) == false)
            return s;
        int idx = s.length() - suffix.length();
        return s.substring(0, idx);
    }

    public String removeExt(String s) {
        String ext = getExt(s);
        if(ext == null)
            return s;
        else
            return removeSuffix(s, ext);
    }

    public String getExt(String s) {
        if(s == null)
            return s;
        int idx = s.lastIndexOf(".");
        return (idx > -1) ? s.substring(idx) : "";
    }

    public boolean existElement(String element, List l) {
        Iterator iter = l.iterator();
        String path;
        while(iter.hasNext()) {
            path = (String)iter.next();
            if(element.equals(path))
                return true;
        }
        return false;
    }

    public List findResourceNames(String prefix, String idxString) {
        int idx = (idxString == null) ? -1 : Integer.parseInt(idxString);
        List matches = new ArrayList();
        Iterator iter = resourceNameList.iterator();
        String name;
        int cnt = 1;
        try {
            while(iter.hasNext()) {
                name = (String)iter.next();
                if(name.startsWith(prefix)){
                    if((idx > -1 && cnt > idx*maxListCount) || idx < 0) {
                        matches.add(name);
                    }
                    cnt++;
                }
                if(idx > -1 && cnt > (idx+1)*maxListCount)
                    break;
            }
            return matches;
        } catch(ConcurrentModificationException cme) {
            return null;
        }
    }

    public boolean existResourceName(String prefix) {
        Iterator iter = resourceNameList.iterator();
        String name;
        try {
            while(iter.hasNext()) {
                name = (String)iter.next();
                if(name.startsWith(prefix))
                    return true;
            }
        } catch(ConcurrentModificationException cme) {
            return false;
        }
        return false;
    }

    public String replace(String s, String source, String target) {
        String ret = s;
        int idx = ret.indexOf(source);
        while(idx > -1) {
            ret = ret.substring(0, idx) + target + ret.substring(idx+source.length());
            idx = ret.indexOf(source, idx+target.length());
        }
        return ret;
    }

    public boolean isEmpty(String sArg) {
        if(sArg == null)
            return true;
        sArg = sArg.trim();
        if(sArg.length() <= 0)
            return true;
        return false;
    }

    public String linkResource(String inputStr, String showStr, boolean findcp) {
        String result = null;
        String first = "<a onMouseOver='showMsg_over(\"Click to view\");' onMouseOut='showmsg_out();' href='?action=view&findcp=" + Boolean.toString(findcp) + "&resource=";
        result = first + inputStr + "'>";
        result += ((showStr == null) ? inputStr : showStr) + "</a>";
        return result;
    }

    public String linkLibraryResource(String inputStr, String showStr) {
        String result = (inputStr.indexOf(".") < 0) ? FQNtoType(inputStr) : inputStr;
        String first = "<a onMouseOver='showMsg_over(\"Click to view\");' onMouseOut='showmsg_out();' href='?action=view&findcp=true&resource=";
        result = first + inputStr + "'>";
        result += ((showStr == null) ? inputStr : showStr) + "</a>";
        return (result==null||result.equals("")) ? "&nbsp;" : result;
    }

    public String linkClass(String inputStr) {
        return linkClass(inputStr, null);
    }

    public String linkClass(String inputStr, String showStr) {
        String result = inputStr;
        result = (result.startsWith("/")) ? result.substring(1) : result;
        result = replace(result, "/", ".");
        String patternStr = "([a-zA-Z0-9_]{1,}\\.){1,}[a-zA-Z0-9]{1,}[@|$][a-zA-Z0-9]{1,}]{0,1}";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inputStr);
        String rep_first = "<a onMouseOver='showMsg_over(\"Click to view\");' onMouseOut='showmsg_out();' href='?action=view&findcp=true&resource=";
        String rep = null;
        String cname = null;
        int cnt = 0;
        while(matcher.find()) {
            cname = matcher.group();
            if(cname.lastIndexOf('@') > 0) {
                cname = cname.substring(0, cname.lastIndexOf('@'));
            }
            rep = rep_first + replace(cname, ".", "/") + "'>" + ((showStr==null)?cname:showStr) + "</a>";
            result = replace(result, cname, rep);
            cnt++;
        }
        if(cnt == 0) {
            patternStr = "([a-zA-Z0-9]{1,}\\.){1,}[a-zA-Z0-9]{1,}";
            pattern = Pattern.compile(patternStr);
            matcher = pattern.matcher(result);
            while(matcher.find()) {
                cname = matcher.group();
                rep = rep_first + replace(cname, ".", "/") + "'>" + ((showStr==null)?cname:showStr) + "</a>";
                result = replace(result, cname, rep);
            }
        }
        return result;
    }

    public String linkClassInfo(String inputStr) {
        return linkClassInfo(inputStr, null);
    }

    public String linkClassInfo(String inputStr, String showStr) {
        String result = FQNtoType(inputStr);
        result = linkClass(result, ((showStr==null)?null:showStr));
        return (result==null||result.equals("")) ? "&nbsp;" : result;
    }

    public String checkLocation(String inputStr) {
        String s = inputStr;
        s = s.startsWith("file://localhost") ? s.substring(16) : s;
        s = s.startsWith("file:") ? s.substring(5) : s;
        s = (!fsep.equals("/")) ? s.substring(1) : s;
        int idx = s.indexOf("!/");
        if(idx > -1) {
            s = s.substring(0, idx) + " >> " + s.substring(idx+2);
        }
        return s;
    }

    public String FQNtoType(String fqn) {
        String retType = null;
        String temp = fqn;
        boolean isArr = false;
        int arrCnt = 0;
        while(temp.startsWith("[")) {
            isArr = true;
            temp = temp.substring(1);
            arrCnt++;
        }
        if(isArr) {
            char t = temp.charAt(0);
            switch(t) {
                case 'B' :
                    retType = "byte"; break;
                case 'C' :
                    retType = "char"; break;
                case 'D' :
                    retType = "double"; break;
                case 'F' :
                    retType = "float"; break;
                case 'I' :
                    retType = "int"; break;
                case 'J' :
                    retType = "long"; break;
                case 'S' :
                    retType = "short"; break;
                case 'Z' :
                    retType = "boolean"; break;
                case 'L' :
                    retType = temp.substring(1, temp.indexOf(';',1)); break;
            }
        }
        if(retType == null) {
            return fqn;
        } else {
            if(isArr) {
                for(int i=0; i<arrCnt; i++) {
                    retType += "[]";
                }
            }
            return retType;
        }
    }

    public String getModifierString(Class cls) {
        String ret = "";
        if(cls == null)
            return ret;
        return getModifierString(cls.getModifiers());
    }

    public String getModifierString(int mod) {
        String ret = "";
        if(mod < 0)
            return ret;
        ret = Modifier.toString(mod);
        return ret;
    }

    public String getNotFoundMessage(String resName, Throwable e) {
        String msg = "<br><i><b>Can't find</b> the resource '<font color=#228B22>" + resName + "</font>' in the classloader.</i><br>";
        if(e != null)
            msg += "<br>Cause : " + e.toString() + "<br>";
        return msg;
    }

    public String getReadErrorMessage(String resName, boolean permissionError) {
        String msg = "<br><i>Can't read the resource '<font color=#228B22>" + resName + "</font>' in absolute path.</i><br>";
        msg += "<br>Cause : ";
        if(permissionError)
            msg += "Permission denied. Can't access the resource.";
        else
            msg += "Not Found. The resource does not exist.";
        msg += "<br>";
        return msg;
    }

    public java.net.URL getResourceURL(String target) {
        return getClass().getResource(((!target.startsWith("/"))?"/":"") + target);
    }

    public String getFileSize(String path) {
        try {
            File f = new File(path);
            long sz = f.length();
            int cnt = 0;
            String unit;
            while(sz >= 1024) {
                sz = sz / 1024;
                cnt++;
            }
            switch(cnt) {
                case 1  : unit = "KB"; break;
                case 2  : unit = "MB"; break;
                case 3  : unit = "GB"; break;
                case 4  : unit = "TB"; break;
                default : unit = "Bytes";
            }
            return sz + " " + unit;
        } catch(Exception e) {
            return "Unknown";
        }
    }

    public String linkDownload(String src, boolean isDir) {
        String rep;
        String res;
        String jar;
        String fsize;
        String ret = "";
        int idx = src.indexOf(" >> ");
        jar = (idx > -1) ? src.substring(0, idx) : null;
        res = (idx > -1) ? src.substring(idx+4) : src;
        fsize = "<b>" + getFileSize((jar==null)?res:jar) + "</b>";
        if(jar != null) {

            rep = "<a class='down' onMouseOver='showMsg_over(\"Click to download - " + fsize + "\");' onMouseOut='showmsg_out();' href='?action=down&type=file&target=" + jar + "'>" + jar + "</a>";
            ret = rep;
        }
        if(!isDir)
            rep = "<a class='down' onMouseOver='showMsg_over(\"Click to download" + ((jar==null)?(" - "+fsize):"") + "\");' onMouseOut='showmsg_out();' href='?action=down&type=" + ((jar==null)?"file":"resource") + "&target=" + replace(res,fsep,"/") + "'>" + res + "</a>";
        else
            rep = res;
        ret += ((jar!=null)?" >> ":"") + rep;
        return ret;
    }

    public void download(InputStream is, HttpServletResponse response, String fname, int fsize) throws Exception {
        if(is == null)
            return;
        try {
            byte b[] = new byte[1024];
            response.reset();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + fname + ";");
            response.setHeader("Content-Transfer-Encoding", "binary;");
            response.setHeader("Content-Encoding" , "identity" );
            if(fsize > 0)
                response.setContentLength(fsize);
            BufferedInputStream fin = new BufferedInputStream(is);
            BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
            int read = 0;
            try {
                while((read = fin.read(b , 0 , 1024) ) != -1){
                    outs.write(b,0,read);
                }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                if(outs!=null) outs.close();
                if(fin!=null) fin.close();
            }
        } catch(Exception e) {
            throw e;
        }
    }

    public void downloadFile(String src, HttpServletResponse response) throws Exception {
        if(src == null)
            return;
        int idx =  src.lastIndexOf("/");
        String fname = (idx > -1) ? src.substring(idx+1) : src;
        File sourcefile = new File(src);
        try {
            if(sourcefile != null && sourcefile.isFile()) {
                FileInputStream fis = new FileInputStream(sourcefile);
                download(fis, response, fname, (int)sourcefile.length());
            }
        } catch(Exception e) {
            throw e;
        }
    }

    public void downloadResource(String target, HttpServletResponse response) throws Exception {
        if(target == null)
            return;
        int idx = target.lastIndexOf("/");
        String fname = (idx > -1) ? target.substring(idx+1) : target;
        java.net.URL resUrl = getResourceURL(target);
        try {
            if(resUrl != null) {
                InputStream is = resUrl.openStream();
                download(is, response, fname, -1);
            }
        } catch(Exception e) {
            throw e;
        }
    }

    public String getHtmlFromString(String sLine) {
        String result = sLine;
        result = replace(result, "&", "&amp;");
        result = replace(result, "<", "&lt;");
        result = replace(result, ">", "&gt;");
        result = replace(result, " ", "&nbsp;");
        result = replace(result, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        return result;
    }

    public String read(Reader reader) {
        if(reader == null)
            return null;
        StringBuffer sRet = new StringBuffer("");
        try {
            BufferedReader br = new BufferedReader(reader);
            String sLine;
            try {
                while((sLine = br.readLine()) != null) {
                    sRet.append(getHtmlFromString(sLine) + "<br>");
                }
            } catch(NullPointerException npe) {
                return null;
            }
            br.close();
            reader.close();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return sRet.toString();
    }

    public String readContent(java.net.URL contentURL, String prefix) {
        StringBuffer sRet = new StringBuffer("");
        if(contentURL == null)
            return null;
        try {
            InputStreamReader isr = new InputStreamReader(contentURL.openStream());
            String s = read(isr);
            if(prefix == null) {
                return s;
            } else {
                int idx = s.indexOf("<br>");
                String linkStr;
                String res;
                boolean isClass;
                while(s.indexOf("<br>") > -1) {
                    idx = s.indexOf("<br>");
                    res = s.substring(0, idx);
                    s = s.substring(idx+4);
                    isClass = res.endsWith(".class");
                    linkStr = prefix + ((prefix.endsWith("/"))?"":"/") + ((isClass)?removeExt(res):res);
                    if(isClass)
                        sRet.append(linkClassInfo(linkStr, res) + "<br>");
                    else
                        sRet.append(linkResource(linkStr, res, true) + "<br>");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return sRet.toString();
    }

    public String readContent(String sFile) throws Exception {
        if(sFile == null || sFile.length() < 1)
            return null;
        File fSrc = new File(sFile);
        if(fSrc == null)
            return null;
        try {
            FileReader fr = new FileReader(fSrc);
            return read(fr);
        } catch(Exception e) {
            throw new Exception("Permission denied. Can't access the file.");
        }
    }

    public String readDirectory(String path) throws Exception {
        if(path == null || path.length() < 1)
            return null;
        StringBuffer sRet = new StringBuffer("");
        File f_path = new File(path);
        if(f_path.exists() == false)
            throw new Exception("Not Found. The directory does not exist.");
        File files[] = f_path.listFiles();
        if(files == null)
            throw new Exception("Permission denied. Can't access the directory.");
        sRet.append("<b>[Directory]</b><br>");
        String parent = f_path.getAbsolutePath();
        parent = (parent.lastIndexOf(fsep) > -1) ? parent.substring(0, parent.lastIndexOf(fsep)) : parent;
        parent = (parent.equals("")) ? ((fsep.equals("/"))?"/":"C:/") : parent;
        sRet.append(linkResource(parent, "..", false) + "<br>");
        String fname;
        String absolute;
        String canonical;
        int cnt = 0;
        for(int i = 0; i < files.length; i++) {
            fname = files[i].getName();
            try {
                if(files[i].isDirectory()) {
                    absolute = replace(files[i].getAbsolutePath(), "\\\\", "\\");
                    canonical = files[i].getCanonicalPath();
                    sRet.append(linkResource(absolute, fname, false));
                    if(!absolute.equals(canonical))
                        sRet.append("&nbsp;&nbsp;->&nbsp;&nbsp;" + linkResource(canonical, canonical, false));
                    sRet.append("<br>");
                }
            } catch(SecurityException se) {}
        }
        for(int i = 0; i < files.length; i++) {
            fname = files[i].getName();
            try {
                if(files[i].isFile()) {
                    if(cnt == 0)
                        sRet.append("<br><b>[File]</b><br>");
                    absolute = replace(files[i].getAbsolutePath(), "\\\\", "\\");
                    canonical = files[i].getCanonicalPath();
                    sRet.append(linkResource(absolute, fname, false));
                    if(!absolute.equals(canonical))
                        sRet.append("&nbsp;&nbsp;->&nbsp;&nbsp;" + linkResource(canonical, canonical, false));
                    sRet.append("<br>");
                    cnt++;
                }
            } catch(SecurityException se) {}
        }
        return sRet.toString();
    }

    public String readLibraryDir(String pkgName) {
        if(pkgName == null || pkgName.length() < 1)
            return null;
        StringBuffer sRet = new StringBuffer("");
        try {
            String prefix = (pkgName.startsWith("/")) ? pkgName.substring(1) : pkgName;
            prefix = (pkgName.endsWith("/")) ? pkgName : (pkgName + "/");
            if(resourceNameList.size() == 0) {
                sRet.append("<font color=red>Resources are not initiated. It will be automatically moved to HOME after five seconds.</font>");
                sRet.append("<script language='javascript'>setTimeout('goHome()',5000)</script>");
            } else {
                List matching = findResourceNames(prefix, null);
                if(matching != null) {
                    String name;
                    if(matching.size() > 0) {
                        Iterator iter = matching.iterator();
                        while(iter.hasNext()) {
                            name = (String)iter.next();
                            sRet.append(linkLibraryResource(name, name.substring(prefix.length())) + "<br>");
                        }
                        matching = null;
                    } else {
                        if(percentComplete > -1 && percentComplete < 100)
                            sRet.append("<font color=red>There is no resource. But resources are still loading.</font>");
                    }
                } else {
                    sRet.append("<font color=red>Resources are now loading. Try again later...</font>");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sRet.toString();
    }

    public int getPackageDepth(String res) {
        String pkg = res.startsWith("/") ? res.substring(1) : res;
        StringTokenizer st = new StringTokenizer(res, "/");
        return st.countTokens();
    }

}
