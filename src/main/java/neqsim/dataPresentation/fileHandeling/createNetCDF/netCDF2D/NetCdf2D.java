/*
 * NetCdf.java
 *
 * Created on 5. august 2001, 21:52
 */

package neqsim.dataPresentation.fileHandeling.createNetCDF.netCDF2D;


import java.io.IOException;
import ucar.ma2.*;
import ucar.nc2.*;
import java.util.*; 
/**
 *
 * @author  esol
 * @version
 */
public class NetCdf2D {

    private static final long serialVersionUID = 1000;
    
    String fileName = "c:/temp/example.nc";
    double[] xvalues = new double[3];
    String xName="xDefault", yName="yDefault", zName = "zDefault";
    double[] yvalues = new double[3];
    double[][] yvalues2 = new double[1000][3];
    String[] yName2 = new String[1000];
    NetcdfFileWriter ncfile;
    int yLength=0;
    
    /** Creates new NetCdf */
    public NetCdf2D() {
    }
    
    public void setOutputFileName(String name){
        fileName = name;
    }
    
    
    public void setXvalues(double[] x, String name, String unit){
        xvalues = x;
        xName = name;
    }
    
    public void setYvalues(double[] y, String name, String unit){
        yvalues = y;
        yName = name;
        
        yvalues2[yLength] = y;
        yName2[yLength] = name;
        yLength++;
    }
    
    
    public void createFile(){
    	
        try {
        NetcdfFileWriter ncfile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, fileName, null);
        }  catch (IOException e) {
            System.err.println("ERROR creating file");
        }
        Dimension latD = ncfile.addDimension(null, xName, xvalues.length);
        ArrayList<Dimension> lonD = new ArrayList<Dimension>();
        
        ArrayList<Dimension> dim2 = new ArrayList<Dimension>();
        dim2.add(latD);
        
        ArrayList<Dimension> tempdim = new ArrayList<Dimension>();
        tempdim.add(latD);
        
        Variable t = ncfile.addVariable(null, latD.getName(), ucar.ma2.DataType.DOUBLE, tempdim);
    //    t.addAttribute(new ucar.nc2.Attribute("units", "degrees_north"));
        
        for(int i=0;i<yLength;i++){
            lonD.add(ncfile.addDimension(null,yName2[i], yvalues2[i].length));
            Variable u = ncfile.addVariable(null, lonD.get(i).getName(), ucar.ma2.DataType.DOUBLE, dim2);
      //      u.addAttribute(new Attribute("units", "degrees_east"));
       
       
        }
        
     //   ncfile.addGroupAttribute(null, new Attribute("title", "Example Data"));
        
        
        
        try {
            ncfile.create();
            
        }  catch (IOException e) {
            
            System.err.println("ERROR creating file");
        }
        
        try {
            Variable v = ncfile.findVariable(latD.getName());
            ncfile.write(v, ArrayDouble.factory(xvalues));
            for(int i=0;i<yLength;i++){
                v = ncfile.findVariable(lonD.get(i).getName());
                ncfile.write(v,  ArrayDouble.factory(yvalues2[i]));
            }
            
        } catch (Exception e) {
            System.err.println("ERROR writing file");
        }
        
        try {
            ncfile.close();
            
        } catch (IOException e) {
        }
        System.out.println("created " + fileName + " successfully");
        
    }
    
    public static void main(String[] args){
        double[] x = new double[10000];//{1,2,3};
        double[] y = new double[10000];
        NetCdf2D test = new NetCdf2D();
        test.setXvalues(x,"time","sec");
        test.setYvalues(y, "length","meter");
        test.createFile();
    }
    
}
