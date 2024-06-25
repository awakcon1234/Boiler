/*
 * Copyright (c) 2023.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.sources;

import com.google.gson.JsonObject;
import net.somewhatcity.boiler.api.CreateArgument;
import net.somewhatcity.boiler.api.CreateCommandArguments;
import net.somewhatcity.boiler.api.IBoilerSource;
import net.somewhatcity.boiler.api.display.IBoilerDisplay;
import net.somewhatcity.boiler.api.util.CommandArgumentType;
import net.somewhatcity.boiler.core.BoilerPlugin;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.LockSupport;

@CreateCommandArguments(arguments = {
        @CreateArgument(name = "url", type = CommandArgumentType.GREEDY_STRING)
})
public class RTMPSource implements IBoilerSource {

    private boolean running;
    private Queue<Short> audioQueue = new ArrayDeque<>();
    //private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private BufferedImage image;
    private AudioFormat SOURCE_FORMAT = new AudioFormat(48000, 16, 1, true, true);
    private final AudioFormat TARGET_FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000F, 16, 1, 2, 48000F, false);

    @Override
    public void load(IBoilerDisplay display, JsonObject data) {
        String streamUrl = data.get("url").getAsString();


        running = true;
        BoilerPlugin.EXECUTOR.execute(() -> {
            try {
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(streamUrl);
                grabber.setOption("listen", "1");
                grabber.setFormat("flv");

                Java2DFrameConverter jconverter = new Java2DFrameConverter();

                grabber.start();
                int leftFrames = 0;

                SOURCE_FORMAT = new AudioFormat(grabber.getSampleRate(), 16, grabber.getAudioChannels(), true, true);
                while (running) {
                    try {
                        long start = System.nanoTime();
                        Frame frame = grabber.grabFrame();
                        if (frame == null) {
                            leftFrames++;
                            if(leftFrames > 100) {
                                System.out.println("skipped over 100 frames. Stopping");
                                break;
                            }
                            continue;
                        }
                        if (frame.samples != null) {
                            ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
                            channelSamplesShortBuffer.rewind();
                            ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);
                            for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
                                short val = channelSamplesShortBuffer.get(i);
                                outBuffer.putShort(val);
                            }
                            byte[] audioData = outBuffer.array();

                            AudioInputStream source = new AudioInputStream(new ByteArrayInputStream(audioData), SOURCE_FORMAT, audioData.length);
                            AudioInputStream converted = AudioSystem.getAudioInputStream(TARGET_FORMAT, source);

                        }

                        if (frame.image != null) {
                            image = jconverter.getBufferedImage(frame);

                            long offset = (long) ((1.0 / grabber.getFrameRate()) * 1000000000);
                            long end = System.nanoTime();
                            long sleep = offset - (end - start);
                            if (sleep > 0) {
                                LockSupport.parkNanos(sleep);
                            }
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                JsonObject err = new JsonObject();
                err.addProperty("message", "Stream stopped");
                display.source("error", err);

            } catch (FFmpegFrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void unload() {
        running = false;
    }

    @Override
    public void draw(Graphics2D g2, Rectangle viewport) {
        g2.drawImage(image, 0, 0, viewport.width, viewport.height, null);
    }

}
