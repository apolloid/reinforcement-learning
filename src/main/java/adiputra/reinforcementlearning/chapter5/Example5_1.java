package adiputra.reinforcementlearning.chapter5;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.EmulGLSkin;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.SurfaceBuilder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import adiputra.reinforcementlearning.App;
import adiputra.utils.*;


public class Example5_1 {
    private final ArrayList<Integer>[][] usableAceStateValueFunction = createValueFunctionArray();
	private final ArrayList<Integer>[][] nonUsableAceStateValueFunction = createValueFunctionArray();
    private int noOfEpisodes;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
	public Example5_1() {
		this.noOfEpisodes = noOfEpisodes;
		this.progressBar = progressBar;
		this.statusLabel = statusLabel;
	}
	
    private ArrayList<Integer>[][] createValueFunctionArray() {
        ArrayList<Integer>[][] array = new ArrayList[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                array[i][j] = new ArrayList<>();
            }
        }
        return array;
    }
    
    
}