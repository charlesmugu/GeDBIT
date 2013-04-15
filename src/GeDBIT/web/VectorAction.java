package GeDBIT.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.opensymphony.xwork2.ActionSupport;

@SuppressWarnings("serial")
public class VectorAction extends ActionSupport
{
    private String xyz;              // 用户提交的坐标和半径
    private double x        = 0;
    private double y        = 0;
    private double z        = 0;
    private double r        = 0;
    private double time     = 0.0001; // 搜索时间
    private int    allcount = 10000; // 搜索数据库的大小

    public void setR(double r)
    {
        this.r = r;
    }

    public double getR()
    {
        return r;
    }

    public void setTime(double time)
    {
        this.time = time;
    }

    public double getTime()
    {
        return time;
    }

    public void setAllcount(int allcount)
    {
        this.allcount = allcount;
    }

    public int getAllcount()
    {
        return allcount;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getX()
    {
        return x;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getY()
    {
        return y;
    }

    public void setZ(double z)
    {
        this.z = z;
    }

    public double getZ()
    {
        return z;
    }

    private Map<String, String> rescoord = new LinkedHashMap<String, String>(); // 用于在JSP显示
    private int                 resnum   = 0;                                  // 结果数量

    public void setRescoord(Map<String, String> rescoord)
    {
        this.rescoord = rescoord;
    }

    public Map<String, String> getRescoord()
    {
        return rescoord;
    }

    public void setResnum(int resnum)
    {
        this.resnum = resnum;
    }

    public int getResnum()
    {
        return resnum;
    }

    public void setXyz(String xyz)
    {
        this.xyz = xyz;
    }

    public String getXyz()
    {
        return xyz;
    }

    class JavaXML // 输出XML 用于和FLASH 传递
    {
        public void BuildXMLDoc() throws IOException, JDOMException
        {

            // 创建根节点 list;
            Element root = new Element("list");

            // 根节点添加到文档中；
            Document Doc = new Document(root);

            // 此处 for 循环可替换成 遍历 数据库表的结果集操作;
            for (int i = 0; i < resnum; i++)
            {

                // 创建节点 user;
                Element elements = new Element("coord");

                // 给 user 节点添加属性 id;
                elements.setAttribute("id", "" + i);

                // 给 user 节点添加子节点并赋值；
                // new Element("name")中的 "name" 替换成表中相应字段，setText("xuehui")中
                // "xuehui
                // 替换成表中记录值；
                // System.out.println(rescoord.get(String.valueOf(i)));
                String[] tp = rescoord.get(String.valueOf(i)).split(", ");

                elements.addContent(new Element("x").setText(tp[0]));
                elements.addContent(new Element("y").setText(tp[1]));
                elements.addContent(new Element("z").setText(tp[2]));
                // 给父节点list添加user子节点;
                root.addContent(elements);

            }
            XMLOutputter XMLOut = new XMLOutputter();

            String file = this.getClass().getClassLoader().getResource("")
                    .getPath();
            // 将%20换成空格（如果文件夹的名称带有空格的话，会在取得的字符串上变成%20）
            file = file.replaceAll("%20", " ");
            file = file.substring(1, file.indexOf("WEB-INF")) + "file.xml"; // 文件路径
            // System.out.println(file);
            // 输出 user.xml 文件；
            // XMLOut.setFormat(Format.getRawFormat());
            XMLOut.setFormat(Format.getPrettyFormat());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // JavaXML x=new JavaXML();
            FileOutputStream fos = new FileOutputStream(file);

            XMLOut.output(Doc, fos);
            // XMLOut.setFormat(Format.getRawFormat());
            // System.out.println(bos.toString());
            bos.close();
            fos.close();
        }

    }

    @SuppressWarnings("unused")
    public void setSour()
    {
        int count = 0;
        String res;
        Pattern patt = Pattern.compile("data:.+"); // 这里生成txt文件
        String fileR = "d:/data/vector/uniformvector-20dim-1m_out.txt"; // 文件路径
        String fileS = "d:/data/vector/uniformvector-20dim-1m.txt"; // 文件路径

        // 下面用于修改插入搜索串

        try
        // 这里读取txt文件
        {
            count = 0;
            File file = new File(fileS);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), "GBK");
                BufferedReader buffreader = new BufferedReader(read);
                String lineTxt = null;
                lineTxt = buffreader.readLine();
                String[] tp = lineTxt.split("   ");
                // String st=tp[1];
                int Acount = Integer.parseInt(tp[1]) + 1;
                FileWriter fileW = new FileWriter(fileR);
                fileW.write(tp[0] + "  " + Acount + "\n");
                fileW.write(x + "  " + y + "  " + z + " " + "\n");
                try
                {
                    while ((lineTxt = buffreader.readLine()) != null)
                    {
                        fileW.write(lineTxt + "\n");
                    }
                } catch (IOException e1)
                {
                    // TODO Auto-generated catch block
                    System.out.println("Writer Error");
                    e1.printStackTrace();

                }
                buffreader.close();
                fileW.flush();
                fileW.close();
                read.close();
            } else
                System.out.println("Open Error");
        } catch (Exception e)
        {
            System.out.println("Error");
        }
    }

    public void setRes()
    {

        int count = 0;
        String res;
        Pattern patt = Pattern.compile("data:.+"); // 这里生成txt文件
        String fileS = "d:/data/vector/vresult.txt"; // 文件路径
        try
        // 这里读取txt文件
        {
            File file = new File(fileS);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), "GBK");
                BufferedReader buffreader = new BufferedReader(read);
                String lineTxt = null;
                buffreader.readLine();
                while ((lineTxt = buffreader.readLine()) != null)
                {
                    Matcher matcher = patt.matcher(lineTxt);
                    while (matcher.find())
                    {
                        res = matcher.group().toString()
                                .replaceFirst("data:", "");
                        res = res.replaceFirst("]", "");
                        rescoord.put(String.valueOf(count), res);
                        // System.out.println(res);
                        ++count;
                    }
                }
                resnum = rescoord.size();
                buffreader.close();
                read.close();
            } else
                System.out.println("Open Error");
        } catch (Exception e)
        {
            System.out.println("Error");
        }
    }

    public String execute() throws Exception
    {
        String[] tp = xyz.split(",");
        x = Double.valueOf(tp[0]);// 获取用户输入x
        y = Double.valueOf(tp[1]);// 获取用户输入y
        z = Double.valueOf(tp[2]);// 获取用户输入z
        r = Double.valueOf(tp[3]);
        setSour();
        new VectorQuery().runQuery(r);
        setRes();
        allcount = 1000;
        time = 0.1;
        try
        {
            // 这里的点是返回的，到时候可以返回一串字符串然后解析成坐标。更改这里的点，在JSP和flash 中都会改变

            JavaXML jx = new JavaXML();
            // System.out.println("生成 mxl 文件...");
            jx.BuildXMLDoc();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return SUCCESS;
    }

}
