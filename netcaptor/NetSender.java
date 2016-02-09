package netcaptor;

import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

import jpcap.*;
import jpcap.NetworkInterface;
import jpcap.packet.*;

public class NetSender {
    public NetSender() {
        createSender();
    }
    
    public void createSender () {
        devices = JpcapCaptor.getDeviceList(); //获取网卡接口列表
        try {
            sender = JpcapSender.openDevice(devices[1]);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "创建数据包发送器失败！");
        }
    }
    
    public void sendTCP(int src_port, int dst_port, long sequence, long ack_num, boolean urg, boolean ack, //TCP首部
                         boolean psh, boolean rst, boolean syn, boolean fin, int window, int urgent,
                         int ttl, InetAddress src, InetAddress dst, //IP首部
                         byte[] src_mac, byte[] dst_mac, //MAC首部
                         String data) throws UnknownHostException {
        
        TCPPacket tcp = new TCPPacket(src_port, dst_port, sequence, ack_num, urg, ack, psh, rst, syn, fin, true, true, window, urgent);
	tcp.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 1010101, ttl, IPPacket.IPPROTO_TCP, src, dst);
	tcp.data = data.getBytes();
		
	EthernetPacket ethernet = new EthernetPacket();
	ethernet.frametype = EthernetPacket.ETHERTYPE_IP;
	ethernet.src_mac = src_mac;
        ethernet.dst_mac = dst_mac;
	tcp.datalink = ethernet;
        
        sender.sendPacket(tcp);
    }
    
    public void sendUDP(int src_port, int dst_port, //UDP首部
                         int ttl, InetAddress src, InetAddress dst, //IP首部
                         byte[] src_mac, byte[] dst_mac, //MAC首部
                         String data) throws UnknownHostException {
        
        UDPPacket udp = new UDPPacket(src_port, dst_port);
        if (src.toString().length()<18 && dst.toString().length()<18) {
            udp.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 1010101, ttl, IPPacket.IPPROTO_UDP, src, dst);
	udp.data = data.getBytes();

	EthernetPacket ethernet = new EthernetPacket();
	ethernet.frametype = EthernetPacket.ETHERTYPE_IP;
	ethernet.src_mac = src_mac;
	ethernet.dst_mac = dst_mac;
	udp.datalink = ethernet;
        
        sender.sendPacket(udp);
        }
	
    }
    
    public void sendARP(int hardtype_num, int operation_num, byte[] target_protoaddr, //ARP首部
                         byte[] src_mac, byte[] dst_mac) { //MAC首部
        
        InetAddress src_ip = null;
	for(NetworkInterfaceAddress addr : devices[2].addresses)
            if(addr.address instanceof Inet4Address) {
		src_ip = addr.address;
		break;
            }
        
        byte[] broadcast = new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
	ARPPacket arp = new ARPPacket();
        if (hardtype_num ==1) {
            arp.hardtype = ARPPacket.HARDTYPE_ETHER;
        }
        else if (hardtype_num == 2) {
            arp.hardtype = ARPPacket.HARDTYPE_IEEE802;
        }
        else {
            arp.hardtype = ARPPacket.HARDTYPE_FRAMERELAY;
        }
	arp.prototype = ARPPacket.PROTOTYPE_IP;
        if (operation_num == 1) {
            arp.operation = ARPPacket.ARP_REQUEST;
        }
        else {
            arp.operation = ARPPacket.ARP_REPLY ;
        }
	arp.hlen = 6;
	arp.plen = 4;
	arp.sender_hardaddr = devices[2].mac_address;
	arp.sender_protoaddr = src_ip.getAddress();
	arp.target_hardaddr = broadcast;
	arp.target_protoaddr = target_protoaddr;
		
	EthernetPacket ethernet = new EthernetPacket();
	ethernet.frametype = EthernetPacket.ETHERTYPE_ARP;
	ethernet.src_mac = devices[2].mac_address;
	ethernet.dst_mac = broadcast;
	arp.datalink = ethernet;
		
	sender.sendPacket(arp);
    }
    
    CaptorFrame frame;
    NetworkInterface[] devices;
    JpcapSender sender;
}
