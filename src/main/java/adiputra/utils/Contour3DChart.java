package adiputra.utils;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.EmulGLSkin;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;


public interface CustomContour3DSurface {
	Shape generateSurface(Number[][] grid);
}

public class Contour3DChart {
	static Quality q = Quality.Nicest();
    
	EmulGLChartFactory factory = new EmulGLChartFactory();
	Chart chart;
    String chartTitle;
	
	public Contour3DChart(
			Number[][] grid,
			CustomContour3DSurface customSurface,
			String chartTitle,
			String xAxisLabel,
			String yAxisLabel,
			String zAxisLabel) {
		this(grid, customSurface, chartTitle, xAxisLabel, yAxisLabel, zAxisLabel, Quality.Nicest());
	}
	
	public Contour3DChart(
			Number[][] grid,
			CustomContour3DSurface customSurface,
			String chartTitle,
			String xAxisLabel,
			String yAxisLabel,
			String zAxisLabel,
			Quality q) {
	    if (customSurface == null) {
	        throw new IllegalArgumentException("CustomContour3DSurface implementation cannot be null.");
	    }
		
		this.chart = this.factory.newChart(q);
		chart.add(customSurface.generateSurface(grid));
		chart.getAxisLayout().setXAxisLabel(xAxisLabel);
		chart.getAxisLayout().setYAxisLabel(yAxisLabel);
		chart.getAxisLayout().setZAxisLabel(zAxisLabel);
		
		this.chartTitle = chartTitle;
	}
	
	public void plotChart() {
		EmulGLSkin skin = EmulGLSkin.on(this.chart);
		skin.getCanvas().setProfileDisplayMethod(true);
        chart.open(this.chartTitle);
        chart.addMouse();
	}
}
