start /b gst-launch-1.0 rtspsrc location=rtsp://10.18.16.16:5600/zed-stream latency=0 ! decodebin ! fpsdisplaysink sync=false & start /b gst-launch-1.0 rtspsrc location=rtsp://10.18.16.16:8554/video latency=0 ! decodebin ! fpsdisplaysink sync=false
