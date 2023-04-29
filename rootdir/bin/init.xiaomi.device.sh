#!/vendor/bin/sh

setprop ro.vendor.xiaomi.device "$(cat /sys/xiaomi-msm8937-mach/codename)"

exit 0
