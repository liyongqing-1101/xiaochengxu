package com.wxjiaozi.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * OSS图片水印工具类
 * 支持文字水印和图片水印
 */
@Slf4j
public class OssWatermarkUtil {

    /**
     * 添加文字水印
     * @param imageStream 原图输入流
     * @param watermarkText 水印文字
     * @return 带水印的图片字节数组 (PNG格式)
     */
    public static byte[] addTextWatermark(InputStream imageStream, String watermarkText) {
        try {
            BufferedImage originalImage = ImageIO.read(imageStream);
            if (originalImage == null) {
                throw new IllegalArgumentException("无法读取图片文件");
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            // 创建画布
            BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = watermarkedImage.createGraphics();

            // 绘制原图
            g2d.drawImage(originalImage, 0, 0, null);

            // 设置水印文字样式
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 字体大小自适应 (图片宽度的5%)
            int fontSize = Math.max(14, width / 25);
            Font font = new Font("SansSerif", Font.PLAIN, fontSize);
            g2d.setFont(font);

            // 文字透明度
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

            // 计算水印位置 (右下角, 留边距)
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(watermarkText);
            int textHeight = fm.getHeight();
            int x = width - textWidth - 20;
            int y = height - textHeight / 2 - 20;

            // 阴影效果
            g2d.setColor(new Color(0, 0, 0, 60));
            g2d.drawString(watermarkText, x + 1, y + 1);

            // 白色文字
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.drawString(watermarkText, x, y);

            g2d.dispose();

            // 输出PNG
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(watermarkedImage, "PNG", bos);
            return bos.toByteArray();

        } catch (Exception e) {
            log.error("添加文字水印失败", e);
            throw new RuntimeException("添加水印失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加图片水印
     * @param imageStream 原图输入流
     * @param watermarkStream 水印图片输入流
     * @param alpha 透明度 0.0-1.0
     * @return 带水印的图片字节数组
     */
    public static byte[] addImageWatermark(InputStream imageStream, InputStream watermarkStream, float alpha) {
        try {
            BufferedImage originalImage = ImageIO.read(imageStream);
            BufferedImage watermarkImage = ImageIO.read(watermarkStream);

            if (originalImage == null || watermarkImage == null) {
                throw new IllegalArgumentException("无法读取图片文件");
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int wmWidth = watermarkImage.getWidth();
            int wmHeight = watermarkImage.getHeight();

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = result.createGraphics();

            // 绘制原图
            g2d.drawImage(originalImage, 0, 0, null);

            // 设置水印透明度
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // 水印位置: 右下角
            int x = width - wmWidth - 20;
            int y = height - wmHeight - 20;

            // 如果水印太大, 缩放到原图的15%
            if (wmWidth > width * 0.3 || wmHeight > height * 0.3) {
                double scale = Math.min(
                        (width * 0.15) / wmWidth,
                        (height * 0.15) / wmHeight
                );
                int newWmWidth = (int) (wmWidth * scale);
                int newWmHeight = (int) (wmHeight * scale);
                x = width - newWmWidth - 20;
                y = height - newWmHeight - 20;
                g2d.drawImage(watermarkImage, x, y, newWmWidth, newWmHeight, null);
            } else {
                g2d.drawImage(watermarkImage, x, y, null);
            }

            g2d.dispose();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(result, "PNG", bos);
            return bos.toByteArray();

        } catch (Exception e) {
            log.error("添加图片水印失败", e);
            throw new RuntimeException("添加水印失败: " + e.getMessage(), e);
        }
    }
}
