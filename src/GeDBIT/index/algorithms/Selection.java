package GeDBIT.index.algorithms ;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
//改一下看看cvs有没有变
public class Selection
{
    
    //返回的pivots[i]应该是x以0开始计数的序号。即第一个x的序号是0
    int m1_p;               //p for model 1
    double m1_x[][];        
    double rss2, rss1;
    int m2_p;               //p for model 2
    double m2_x[][];
    double x[][];           //array that comes from arguments
    double y[][];           //array that comes from arguments
    DoubleMatrix2D y_matrix;    //will be used when calculate the init rss
    int p;
    int n;
    boolean choose[];
    int pnum;
    int testSign;   //判定用什么评判标准（F-test or sth.）
    
    //boolean t_choose[];     //在枚举法的时候用到的记录数组，用来记录某个点是否选过
    int chose_x[];          //记录选到的点
    int result_x[];         //记录结果
    //double minRss;          //枚举法
    
    double maxf;    //used in forward
    double minf;    //used in backward
    int nextv;      //used in both f and b
    double temprss; 
    double minrss;  //used in f, b, and enum
    
    void setTestSign(int s)
    {
        testSign = s;
    }
    
    public Selection(double[][] matrix_x, double[][] matrix_y, int num)
    {
        x = matrix_x;
        y = matrix_y;
        pnum = num;
        
        y_matrix = new DenseDoubleMatrix2D(y);
        n = y_matrix.rows();
        DoubleMatrix2D x_matrix = new DenseDoubleMatrix2D(x);
        p = x_matrix.columns()-1;
        
        for(int i=0; i<n; i++)
            x[i][0] = 1;
    }
    
    double calF(int v)
    {
        //rss2 = calRss(m2x, m2p);
        //int v = n-m2p-1;
        return (rss1-rss2)/(rss2/v);
    }

    double calRss(double[][] m_x, int m_p)
    {
        Algebra alg = new Algebra();
        //计算beta, 
        DoubleMatrix2D model_x = new DenseDoubleMatrix2D(m_x);

        DoubleMatrix2D transpose_matrix = alg.transpose(model_x);  //x'

        DoubleMatrix2D mul_matrix = alg.mult(transpose_matrix, model_x);   //x'x

        DoubleMatrix2D inverse_matrix = alg.inverse(mul_matrix);    //(x'x)-1
        //print(inverse_matrix);
        //System.out.println();
        mul_matrix = alg.mult(inverse_matrix, transpose_matrix);    //(x'x)-1x'
        //print(mul_matrix);
        //System.out.println();
        DoubleMatrix2D beta = alg.mult(mul_matrix, y_matrix);       //(x'x)-1x'y
        //print(beta);
        //System.out.println();

        //计算rss
        mul_matrix = alg.mult(model_x, beta);          //xbeta
        //print(mul_matrix);
        //System.out.println();
        DoubleMatrix2D temp_matrix = new DenseDoubleMatrix2D(n, 1); //y-xbeta
        double k;
        for(int i=0; i<n; i++){
            k = y_matrix.getQuick(i, 0) - mul_matrix.getQuick(i, 0);
            temp_matrix.setQuick(i, 0, k);
        }
        //print(temp_matrix);
        //System.out.println();
        //inverse_matrix = alg.inverse(temp_matrix);                  //(y-xbeta)'
        transpose_matrix = alg.transpose(temp_matrix);                //(y-xbeta)'
        //print(transpose_matrix);
        //System.out.println();
        mul_matrix = alg.mult(transpose_matrix, temp_matrix);         //(y-xbeta)'(y-xbeta)
        //print(mul_matrix);
        //System.out.println();
        return mul_matrix.getQuick(0, 0);
    }
    
    int forwardInit()
    {

        m1_p = 0;           //model1一开始没有变量
        m2_p = 1;           //model2一开始有一个变量
        //choose数组，用来标志哪个变量已经选入
        choose = new boolean[p+1];
        for(int i=0; i<=p; i++)
            choose[i] = false;

        //初始model中只有beta一项，全1
        m2_x = new double[n][2];
        for(int i=0; i<n; i++)
            m2_x[i][0] = x[i][0];
        
        //算初始rss1，就是第一个只有beta0项的空模型的rss
        /*
         * 因为rss是针对每个模型。
         * 所以在每一步循环中，model1的rss就是上一步选出来的model2的rss。
         * 所以可以把上一步的rss记录下来，就不用在每次循环中再次计算rss1了。
         */
        double avgy = y_matrix.zSum()/y_matrix.size();          //beta = avgy
        
        DoubleMatrix2D minus_matrix = new DenseDoubleMatrix2D(n, 1);    //y-beta
        for(int i=0; i<n; i++)
        {
            double temp = y_matrix.get(i, 0) - avgy;        //y-beta
            minus_matrix.set(i, 0, temp);
        }
        DoubleMatrix2D transpose_minus = (new Algebra()).transpose(minus_matrix);   //(y-beta)T
        DoubleMatrix2D mult = (new Algebra()).mult(transpose_minus, minus_matrix);  //(y-beta)T*(y-beta)
        rss1 = mult.getQuick(0, 0);
        //rss2会在以后算
        rss2 = 0;
        return 0;
    }
    
    boolean forwardEnd()
    {
        //这个是判定结束条件
        if(m2_p > pnum)
            return true;
        return false;
    }
    
    int[] forwardSelection()
    {
        forwardInit();
        //计算函数
        while(!forwardEnd())
        {
            //这三个是用来记录最大值以及得出最大值的变量
            //double maxf = 0;
            //int nextv = 0;
            //double temprss = 0.0;
            maxf = 0;
            nextv = 0;
            temprss = 0;
            minrss = 999999999E100;
            for(int i=1; i<=p; i++)
            {        //找下一个变量
                if(choose[i])               //找出一个没选过的
                    continue;
    
                for(int j=0; j<n; j++)      //把下个变量的x列加入到m2模型中
                    m2_x[j][m2_p] = x[j][i];
                forwardTest(m2_x, m2_p, i); //传入到检测函数
            }
            choose[nextv] = true;           //把找出来的x标记为已选
            m1_p = m2_p;                    //把model2变成下一轮的model1
            m1_x = new double[n][m1_p+1];
            for(int i=0; i<n; i++)
                for(int j=0; j<m1_p; j++)
                    m1_x[i][j] = m2_x[i][j];    
            for(int i=0; i<n; i++)          //最后一列要变成我们选中的x列
                m1_x[i][m1_p] = x[i][nextv];
            
            m2_p++;                         //model2的变量个数要增加
            m2_x = new double[n][m2_p+1];
            for(int i=0; i<n; i++)
                for(int j=0; j<m2_p; j++)
                    m2_x[i][j] = m1_x[i][j];
            
            rss1 = temprss;                    //rss互换，下次就不用再计算了
        }
        int[] result = new int[pnum];
        int j = 0;
        for(int i=1; i<=p; i++)
            if(choose[i])
                result[j++] = i-1;

        return result;
    }
    
    void forwardTest(double[][] m2x, int m2p, int point)
    {
        //double result = 0;
        if(testSign == 1)
        {
            rss2 = calRss(m2x, m2p);
            int v = n-m2p-1;
            //result = calF(v);
            double f = calF(v);
            if(f>maxf)
            {
                maxf = f;
                nextv = point;
                temprss = rss2;
            }
        }
        else if(testSign == 2)
        {
            rss2 = calRss(m2x, m2p);
            if(rss2<minrss)
            {
                minrss = rss2;
                nextv = point;
            }
        }
        //return result;
    }
    
    /*
    double testModel(double[][] m2x, int m2p){
        //根据testSign的值来选择用什么方法来检测
        double result = 0;
        if(testSign == 1)
            result = calF(m2x, m2p);
        return result;
    }
    */
    /*
    double calF(double[][] m2x, int m2p){
        Algebra alg = new Algebra();
        //计算beta, 
        DoubleMatrix2D model2_x = new DenseDoubleMatrix2D(m2x);
        //System.out.println("m2");
        //print(model2_x);
        DoubleMatrix2D transpose_matrix = alg.transpose(model2_x);  //x'
        //System.out.println("trans");
        //print(transpose_matrix);
        DoubleMatrix2D mul_matrix = alg.mult(transpose_matrix, model2_x);   //x'x
        //System.out.println("mul_matrix");
        //print(mul_matrix);
        DoubleMatrix2D inverse_matrix = alg.inverse(mul_matrix);    //(x'x)-1
        //print(inverse_matrix);
        //System.out.println();
        mul_matrix = alg.mult(inverse_matrix, transpose_matrix);    //(x'x)-1x'
        //print(mul_matrix);
        //System.out.println();
        DoubleMatrix2D beta = alg.mult(mul_matrix, y_matrix);       //(x'x)-1x'y
        //print(beta);
        //System.out.println();

        //计算rss
        mul_matrix = alg.mult(model2_x, beta);          //xbeta
        //print(mul_matrix);
        //System.out.println();
        DoubleMatrix2D temp_matrix = new DenseDoubleMatrix2D(n, 1); //y-xbeta
        double k;
        for(int i=0; i<n; i++){
            k = y_matrix.getQuick(i, 0) - mul_matrix.getQuick(i, 0);
            temp_matrix.setQuick(i, 0, k);
        }
        //print(temp_matrix);
        //System.out.println();
        //inverse_matrix = alg.inverse(temp_matrix);                  //(y-xbeta)'
        transpose_matrix = alg.transpose(temp_matrix);                //(y-xbeta)'
        //print(transpose_matrix);
        //System.out.println();
        mul_matrix = alg.mult(transpose_matrix, temp_matrix);         //(y-xbeta)'(y-xbeta)
        //print(mul_matrix);
        //System.out.println();
        rss2 = mul_matrix.getQuick(0, 0);
        
        int v = n-m2p-1;
        return (rss1-rss2)/(rss2/v);
    }
    */
    /*
    boolean bigger(double v1, double v2){
        //判断double型的大小
        BigDecimal d1 = new BigDecimal(v1);
        BigDecimal d2 = new BigDecimal(v2);
        if(d1.compareTo(d2)>0)
            return true;
        else
            return false;
    }
    */

    int[] enumerateSelection()
    {
        //比较rss,选出最小的rss
        enumerateInit();
        //当前已经选了0个点，第一个点是0
        enumerateRecurSelect(0, 0);
        //算出来以后result_x里就是所选的点
        
        int[] result = new int[pnum];
        for(int i=1; i<=pnum; i++)
            result[i-1] = result_x[i]-1;
        /*
        System.out.println("result:");
        for(int i=0; i<pnum; i++)
            System.out.print(result[i]+" ");
        System.out.println();
        */
        return result;
    }
    
    //num表示现在已经选了多少个点，point表示上次选的点
    //记着，选的点是从1开始计数的
    void enumerateRecurSelect(int num, int point)
    {
        if(num >= pnum)             //选够点了，算rss
        {
            double rss = enumerateTest(chose_x);
            if(rss < minrss)        //比较，记录最小的rss，保存数组
            {
                for(int i=0; i<=pnum; i++)
                    result_x[i] = chose_x[i];
                minrss = rss;
            }
        }
        else                        //没选够点，选下一个点
        {
            for(int i=point+1; i<=p; i++)   //枚举下一个点
            {
                //t_choose[i] = true;
                chose_x[num+1] = i;         //加入现在枚举的点
                enumerateRecurSelect(num+1, i);     //进入下一轮递归
                //t_choose[i] = false;
                chose_x[num+1] = 0;         //回溯，还原（其实应该不用）
            }
        }
    }
    
    double enumerateTest(int[] arr)
    {
        //先把各个点提取出来，组成x矩阵
        //一个n行pnum+1列的矩阵（因为只选择了pnum个参数加入进去，加上x0的列）
        double[][] enum_x = new double[n][pnum+1];
        //int x_i = 0;                //enum_x的计数器
        for(int i=0; i<=pnum; i++)
            for(int j=0; j<n; j++)
                enum_x[j][i] = x[j][arr[i]];
        return calRss(enum_x, pnum);
    }
    
    /*
    double enumerateCalRss(int[] arr)
    {
        //先把各个点提取出来，组成x矩阵
        //一个n行pnum+1列的矩阵（因为只选择了pnum个参数加入进去，加上x0的列）
        double[][] enum_x = new double[n][pnum+1];
        //int x_i = 0;                //enum_x的计数器
        for(int i=0; i<=pnum; i++)
            for(int j=0; j<n; j++)
                enum_x[j][i] = x[j][arr[i]];

        //然后通过x矩阵算出rss
        Algebra alg = new Algebra();
        //beta
        DoubleMatrix2D model_x = new DenseDoubleMatrix2D(enum_x);   //x
        DoubleMatrix2D transpose_matrix = alg.transpose(model_x);   //x'
        DoubleMatrix2D mul_matrix = alg.mult(transpose_matrix, model_x);    //x'x
        //System.out.println("mul_matrix");
        //print(mul_matrix);
        DoubleMatrix2D inverse_matrix = alg.inverse(mul_matrix);    //(x'x)-1
        mul_matrix = alg.mult(inverse_matrix, transpose_matrix);    //(x'x)-1x'
        DoubleMatrix2D beta = alg.mult(mul_matrix, y_matrix);       //(x'x)-1x'y
        
        //rss
        mul_matrix = alg.mult(model_x, beta);   //xbeta
        DoubleMatrix2D temp_matrix = new DenseDoubleMatrix2D(n, 1); //y-xbeta
        double k;
        for(int i=0; i<n; i++)
        {
            k = y_matrix.getQuick(i, 0) - mul_matrix.getQuick(i, 0);
            temp_matrix.setQuick(i, 0, k);
        }
        transpose_matrix = alg.transpose(temp_matrix);          //(y-xbeta)'
        mul_matrix = alg.mult(transpose_matrix, temp_matrix);   //(y-xbeta)'(y-xbeta)
        
        return mul_matrix.getQuick(0, 0);
    }
    */
    
    int enumerateInit()
    {
        /*
        y_matrix = new DenseDoubleMatrix2D(y);
        n = y_matrix.rows();
        DoubleMatrix2D x_matrix = new DenseDoubleMatrix2D(x);
        p = x_matrix.columns()-1;
        */
        /*
        for(int i=1; i<n; i++)
            x[i][0] = 1;
        */
        minrss = 999999999;
        chose_x = new int[pnum+1];
        result_x = new int[pnum+1];
        //choose = new boolean[p+1];

        chose_x[0] = 0;             //第一个为0，就是一定要把第一个常数项选中
        return 0;
    } 

    int backwardInit()
    {
        m1_p = p-1;
        m2_p = p;
        choose = new boolean[p+1];
        for(int i=0; i<=p; i++)
            choose[i] = false;
        
        //m1只要new就好了，到后面计算再赋值
        m1_x = new double[n][m1_p+1];

        rss2 = calRss(x, p);
        rss1 = 0.0;
        return 0;
    }
    
    boolean backwardEnd()
    {
        //m2_p代表了此阶段模型里的变量数目。当m2_p等于pnum的时候，结束。
        if(m2_p > pnum)
            return false;
        return true;
    }

    void backwardTest(double[][] m1x, int m1p, int point)
    {
        if(testSign == 1)           //f-test
        {
            rss1 = calRss(m1x, m1p);
            int v = n-m2_p-1;
            double f = calF(v);
            //System.out.println(f);
            if(f<minf)
            {
                minf = f;
                nextv = point;
                temprss = rss1;
            }
        }
        else if (testSign == 2)     //rss
        {
            rss1 = calRss(m1x, m1p);
            if(rss1<minrss)
            {
                minrss = rss1;
                nextv = point;
            }
        }
    }

    int[] backwardSelection()
    //因为大模型为m2，小模型为m1。
    //所以这里上一步的模型为m2，下一步的模型为m1。
    //和forward正好相反
    {
        backwardInit();
        while(!backwardEnd())
        {
            //debugPrint();
            //System.out.println("in selection");
            //double minf = 999999999;
            //int nextv = 0;
            //double temprss = 0.0;
            minf = 999999999E100;
            minrss = 999999999E100;
            nextv = 0;
            temprss = 0;
            for(int i=1; i<=p; i++)
            {
                if(choose[i])
                    continue;
                //把所有没有选择丢弃的变量拷到m1中
                //即所有choose[j]为false的变量
                int k=0;
                //System.out.println("m1p :"+m1_p);
                //System.out.println("p :"+p);
                //System.out.println("I :"+i);
                //System.out.println();
                for(int j=0; j<=p; j++)
                {
                    if (!(choose[j])&&(j!=i))            //当前选择的也不拷贝
                    {
                        //System.out.println("j :"+j);
                        //System.out.println("k :"+k);
                        for(int row=0; row<n; row++)        //把j列拷贝过去
                            m1_x[row][k] = x[row][j];
                        k++;
                    }
                }
                //double f = backwardTest(m1_x, m1_p);
                backwardTest(m1_x, m1_p, i);
                /*if(f<minf)
                {
                    minf = f;
                    nextv = i;
                    temprss = rss1;
                }*/
            }
            choose[nextv] = true;
            //System.out.println("choose: "+nextv);
            //System.out.println();
            //不用把model1拷成model2，只要把rss记录下来就好了。m2_p也要变化。
            m2_p--;
            m1_p--;
            m1_x = new double[n][m1_p+1];
            rss2 = temprss;
            //System.out.println("choose "+nextv);
            //System.out.println("rss is :"+rss1);
            //System.out.println();
        }
        int[] result = new int[pnum];
        int j = 0;
        for(int i=1; i<=p; i++)
            if(!choose[i])              //没有被选中，剩下的才是我们想要的
                //result[j++] = i;
                result[j++] = i-1;
        /*
        System.out.println("result:");
        for(int i=0; i<result.length; i++)
            System.out.print(result[i]+" ");
        System.out.println();
        */
        return result;
    }

    void debugPrint()
    {
        System.out.println("choose array: ");
        for(int i=0; i<=p; i++)
            if(!choose[i])
                System.out.print(i+" ");
        System.out.println();
        //System.out.println("")
    }
    
    void print(final DoubleMatrix2D matrix)
    {
        //这是用来调试的函数
        for(int i=0; i<matrix.rows(); i++)
        {
            for(int j=0; j<matrix.columns(); j++)
                System.out.print(matrix.getQuick(i, j)+" ");
            System.out.println(); 
        }
        System.out.println("row :"+matrix.rows());
        System.out.println("col :"+matrix.columns());
    }
}