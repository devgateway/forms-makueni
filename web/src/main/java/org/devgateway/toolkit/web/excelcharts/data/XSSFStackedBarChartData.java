package org.devgateway.toolkit.web.excelcharts.data;

import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.charts.ChartAxis;

/**
 * @author idobre
 * @since 8/8/16
 *
 *        Holds data for a XSSF Stacked Bar Chart.
 */
public class XSSFStackedBarChartData extends XSSFBarChartData {
    //private STBarGrouping.Enum barGrouping = STBarGrouping.STACKED;

    private static final int OVERLAPPERCENTAGE = 100;

    public XSSFStackedBarChartData(final String title) {
        super(title);
    }

    @Override
    public void fillChart(final Chart chart, final ChartAxis... axis) {
//        if (!(chart instanceof XSSFChart)) {
//            throw new IllegalArgumentException("Chart must be instance of XSSFChart");
//        }
//
//        final XSSFChart xssfChart = (XSSFChart) chart;
//        final CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
//        final CTBarChart barChart = plotArea.addNewBarChart();
//
//        barChart.addNewVaryColors().setVal(false);
//
//        // create a stacked bar
//        barChart.addNewGrouping().setVal(barGrouping);
//        barChart.addNewOverlap().setVal((byte) OVERLAPPERCENTAGE);
//
//        // set bars orientation
//        barChart.addNewBarDir().setVal(barDir);
//
//        xssfChart.setTitleText(this.title);
//
//        CTValAx[] ctValAx = plotArea.getValAxArray();
//        if (ctValAx.length != 0) {
//            ctValAx[0].addNewMajorGridlines().addNewSpPr().addNewSolidFill();
//            ctValAx[0].getCrossBetween().setVal(STCrossBetween.BETWEEN);
//        }
//
//        for (CustomChartSeries s : series) {
//            s.addToChart(barChart);
//        }
//
//        for (ChartAxis ax : axis) {
//            barChart.addNewAxId().setVal(ax.getId());
//        }
    }

//    public void setBarGrouping(final STBarGrouping.Enum barGrouping) {
//        this.barGrouping = barGrouping;
//    }
}
