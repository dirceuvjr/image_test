package segmentador;

import org.bytedeco.javacpp.Loader;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.cvRelease;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.io.FileFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

public class Teste {

    public void teste() throws IOException {

        final FileFilter pngFileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getPath().endsWith("png");
            }
        };

        final File fList[] = new File(getClass().getClassLoader().getResource("png").getFile()).listFiles(pngFileFilter);

        new File(getClass().getClassLoader().getResource("png").getFile() + "/processadas").mkdir();

        for (File img : fList) {
            final long start = System.currentTimeMillis();
            processImage(img);
            System.out.println("Elapsed time " + (System.currentTimeMillis() - start) + "ms.");
        }
    }

    private void processImage(File img) {
        final String path = img.getAbsolutePath();
        final String ext = img.getName().split("\\.")[1];
        final String name = img.getName().split("\\.")[0];

        if ("CR2".equals(ext)) return;

        final String caminho = img.getAbsolutePath().replace(img.getName(), "");
        System.out.println(path);

        final IplImage imagem = cvLoadImage(path);

//                imprimeHistogramaRGB(imagem);

        final IplImage gray = cvCreateImage(cvGetSize(imagem), IPL_DEPTH_8U, 1);
        cvCvtColor(imagem, gray, CV_RGB2GRAY);

//                imprimeHistograma(gray);

        final IplImage bin = cvCreateImage(cvGetSize(gray), IPL_DEPTH_8U, 1);
        cvThreshold(gray, bin, 0, 255, CV_THRESH_OTSU);

//                imprimeHistograma(bin);

        final IplImage bin2 = cvCreateImage(cvGetSize(gray), IPL_DEPTH_8U, 1);
        cvThreshold(gray, bin2, 180, 256, CV_THRESH_BINARY);

        int[] histogramaBin2 = calculaHistograma(bin2);
        System.out.println("Brancos " + histogramaBin2[255]);
        System.out.println("Pretos " + histogramaBin2[0]);

//                imprimeHistograma(bin2);

        cvSaveImage(caminho + "processadas/" + name + "_gray.png", gray);
        cvSaveImage(caminho + "processadas/" + name + "_bin_otsu.png", bin);
        cvSaveImage(caminho + "processadas/" + name + "_bin_limiar.png", bin2);

        cvRelease(gray);

        System.out.println("--Otsu");
        cvSaveImage(caminho + "processadas/" + name + "_contorno_otsu.png", contorno(imagem.clone(), bin));

        System.out.println("--Limiar");
        cvSaveImage(caminho + "processadas/" + name + "_contorno_limiar.png", contorno(imagem.clone(), bin2));

        cvRelease(imagem);
        cvRelease(bin);
        cvRelease(bin2);

        System.out.println("----");
    }

    private IplImage contorno(IplImage imageContorno, IplImage bin) {
        CvMemStorage memoria = CvMemStorage.create();
        CvSeq contornos = new CvSeq();
        int qntContornos = cvFindContours(bin, memoria, contornos, Loader.sizeof(CvContour.class), CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);

        double area_ruido = 50.0;
        int area_total = 0;
        int qnt = 0;

        for (CvSeq contorno = contornos; contorno != null && qntContornos > 0 && contornos != null; contorno = contorno.h_next()) {
            double area = cvContourArea(contorno, CV_WHOLE_SEQ, 0);
            if (area > area_ruido) {
                area_total += area;
                cvDrawContours(imageContorno, contorno, CV_RGB(120, 12, 12), CV_RGB(2, 27, 242), -1, 1, 8);
                qnt++;
            }
        }
        System.out.println("Area total = " + area_total);
        System.out.println("Quantidade = " + qnt);

        memoria.release();

        return imageContorno;
    }


    private int[][] calculaHistogramaRGB(IplImage image) {
        CvScalar v;
        int[][] h = new int[3][256];
        int R, G, B, i, j, k;

        for (j = 0; j < 3; j++) {
            for (k = 0; k < 256; k++) {
                h[j][k] = 0;
            }
        }

        for (i = 0; i < image.height(); i++) {
            for (j = 0; j < image.width(); j++) {
                v = cvGet2D(image, i, j);
                R = (int) v.val(0);
                G = (int) v.val(1);
                B = (int) v.val(2);

                h[0][R]++;
                h[1][G]++;
                h[2][B]++;

            }
        }
        return h;
    }

    private void imprimeHistogramaRGB(IplImage image) {
        imprimeHistogramaRGB(calculaHistogramaRGB(image));
    }

    private void imprimeHistogramaRGB(int[][] histograma) {
        int j, k;
        for (j = 0; j < 3; j++) {
            for (k = 0; k < 256; k++) {
                System.out.println(histograma[j][k]);
            }
        }
    }

    private int[] calculaHistograma(IplImage image) {
        CvScalar v;
        int[] h = new int[256];
        int R, i, j, k;

        for (k = 0; k < 256; k++) {
            h[k] = 0;
        }

        for (i = 0; i < image.height(); i++) {
            for (j = 0; j < image.width(); j++) {
                v = cvGet2D(image, i, j);
                R = (int) v.val(0);

                h[R]++;
            }
        }
        return h;
    }

    private void imprimeHistograma(IplImage image) {
        imprimeHistograma(calculaHistograma(image));
    }
    private void imprimeHistograma(int[] histograma) {
        int k;
        for (k = 0; k < 256; k++) {
            System.out.println(k + ";" + histograma[k]);
        }
    }
}
