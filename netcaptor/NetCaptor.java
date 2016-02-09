package netcaptor;

import java.awt.Font;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;
import java.io.*;

import javax.swing.UIManager;
import jpcap.*;
import jpcap.packet.*;

class NetCaptor {     
    public NetCaptor(CaptorFrame frame,int deviceIndex) {
        this.frame = frame;
        this.deviceIndex=deviceIndex;
        setFont();
        createCaptor();
        iniAddressInfo();
        createCaptureThread();
    }
    
    public void setFont() {
        Font font = new Font("微软雅黑" , 1, 18); 
        UIManager.put("OptionPane.font", font); 
        UIManager.put("OptionPane.messageFont", font); 
        UIManager.put("OptionPane.buttonFont", font); 
    }
    
    public void createCaptor() {
        devices = JpcapCaptor.getDeviceList(); //获取网卡接口列表
        try {
            captor = JpcapCaptor.openDevice(devices[deviceIndex], 1518, true, 0); //默认使用第一个网卡接口，数据包的最大长度1518字节
        }
        catch (IOException io_exception) {
            JOptionPane.showMessageDialog(frame, "打开网卡接口出错！");
            System.exit(-1); //关闭程序
        }
    }
    
    public void iniAddressInfo() {
        frame.setAddress(macToHexString(devices[deviceIndex].mac_address), getHostIP()); //在界面上显示本机MAC地址和IP地址
    }
    
    public static String macToHexString(byte[] src) { //将字节转换成16进制
        StringBuilder stringbuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i<src.length; i++) {  
            String hv = Integer.toHexString(src[i] & 0xFF);  
            if (hv.length()<2) {  
                stringbuilder.append(0);  
            }  
            stringbuilder.append(hv + ":"); 
        }  
        return stringbuilder.toString().substring(0, 17); //去掉最后的冒号
    }  
    
    public String getHostIP() {
        String address = null;
        try {
            address = InetAddress.getLocalHost().getHostAddress(); //获取本机IP地址
        }
        catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(frame, "无法获取本机IP地址！");
        }
        return address;
    }
    
    public void createCaptureThread() {
        new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        while (keep_capturing) { 
                            captor.processPacket(1, new PacketReceiver() { //抓到一个包就返回
                                    public void receivePacket(Packet packet) {
                                        frame.dealPacket(packet); //处理抓到的这个包
                                        captor.updateStat(); //后台显示抓包、丢包数量
                                        System.out.println("抓包：" + captor.received_packets);
                                        System.out.println("丢包：" + captor.dropped_packets);
                                    }
                                });
                            try {
                                Thread.sleep(0);
                            }
                            catch (Exception e) {
                            }
                        }
                    }
                }
            }).start();
    }
    
    public void startSniff() {
        keep_capturing = true; //不停抓取数据包
    }
    
    public void stopSniff() {
        keep_capturing = false; //暂停抓取数据包
    }
    
    public JpcapCaptor getCaptor() {
        return captor;
    }
    
    CaptorFrame frame;
    NetworkInterface[] devices;
    JpcapCaptor captor;
    int deviceIndex;
    boolean keep_capturing = false;
    
}
