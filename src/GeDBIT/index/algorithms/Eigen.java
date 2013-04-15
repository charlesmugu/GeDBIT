package GeDBIT.index.algorithms ;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
//import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

public class Eigen
{
    double x[][];
    DoubleMatrix2D x_matrix;
    DoubleMatrix2D eigen_matrix;
    float float_positive_result[];
    float sort_result[];
    
    //construction function
    public Eigen(double[][] m_x)
    {
        
        x = m_x;
        x_matrix = new DenseDoubleMatrix2D(x);
        float_positive_result = new float[x_matrix.columns()];
        sort_result = new float[x_matrix.columns()];
    }
    
    public int calEigen()
    {
        EigenvalueDecomposition eigen_cal = new EigenvalueDecomposition(x_matrix);
        eigen_matrix = eigen_cal.getD();
        @SuppressWarnings("unused")
        int[] result = getEigen();
        sort();
        return 0;
    }
    
    public int[] getEigen()
    {
        int[] result = new int[eigen_matrix.columns()];
        for(int i=0; i<eigen_matrix.columns(); i++)
        {
            //result[i] = eigen_matrix.get(i, i);
            Double double_temp = eigen_matrix.get(i, i);
            if(double_temp<0)
                double_temp = -double_temp;
            Float float_temp = double_temp.floatValue();
            
            float_positive_result[i] = float_temp;
            result[i] = float_temp.intValue();
        }
        
        return result;
    }
    
    public int printEigenDouble()
    {
        //int i = 0;
        for(int i=0; i<eigen_matrix.columns(); i++)
            System.out.println(eigen_matrix.get(i, i)+" ");
        System.out.println();
        return 0;
    }
    
    public int printEigenFloat()
    {
        for(int i=0; i<eigen_matrix.columns(); i++)
        {
            Double temp_double = eigen_matrix.get(i, i);
            System.out.printf("%.3f",temp_double.floatValue());
            //System.out.print(" ");
            System.out.println();
        }
        System.out.println();
        return 0;
    }
    
    public int printEigenFloatPositive()
    {
        for(int i=0; i<eigen_matrix.columns(); i++)
        {
            //Double temp_double = eigen_matrix.get(i, i);
            //if(temp_double<0)
            //    temp_double = -temp_double;
            //System.out.printf("%.3f\n", temp_double.floatValue());
            //float_positive_result[i] = temp_double.floatValue();
            System.out.printf("%.3f\n", float_positive_result[i]);
        }
        return 0;
    }
    
    public int sort()
    {
        int n = eigen_matrix.columns();
        float temp = 0;
        for(int i=0; i<n; i++)
            sort_result[i] = float_positive_result[i];
        for(int i=0; i<n; i++)
            for(int j=i+1; j<n; j++)
                if(sort_result[i]<sort_result[j])
                {
                    temp = sort_result[i];
                    sort_result[i] = sort_result[j];
                    sort_result[j] = temp;
                }
        
        //for(int i=0; i<n; i++)
        //    System.out.printf("%.3f\n", sort_result[i]);
        return 0;
    }
    
    public int calPivotNumber()
    {
        int n = eigen_matrix.columns();
        double maxratio = 0.0;
        int numPivot = 0;
        for(int i=1; i<n-1; i++)
        {
            double ratio = sort_result[i]/sort_result[i+1];
            if(ratio > maxratio)
            {
                maxratio = ratio;
                numPivot = i;
            }
        }
        return numPivot+1;
    }
    
    public int printBiggestEigen(int n)
    {
        //sort();
        for(int i=0; i<n; i++)
            System.out.printf("%.3f\n", sort_result[i]);
            //System.out.printf("%.3f\n", float_positive_result[i]);
        return 0;
    }
    
    
    public int printSortResult()
    {
        int n = eigen_matrix.columns();
        System.out.println("Eigen result::::::");
        for(int i=0; i<n; i++)
            System.out.println(sort_result[i]);
        System.out.println("::::::::::::::::");
        return 0;
    }
    
}