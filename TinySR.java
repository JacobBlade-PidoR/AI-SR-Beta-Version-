package com.jacob.aisrlite;

import android.graphics.Bitmap;

public final class TinySR {
    private static int clamp(int x){return x<0?0:(x>255?255:x);}
    private static float relu(float x){return x>0?x:0;}
    public static Bitmap infer(Bitmap src){
        final int w=src.getWidth(), h=src.getHeight(), N=w*h;
        int[] px=new int[N]; src.getPixels(px,0,w,0,0,w,h);
        float[] in=new float[N*3];
        for(int i=0;i<N;i++){int c=px[i]; in[i]=((c>>16)&255)/255f; in[N+i]=((c>>8)&255)/255f; in[2*N+i]=(c&255)/255f;}
        float[] a=new float[N*8], b=new float[N*8], out=new float[N*3];
        conv(in,a,w,h,3,8,TinySRModel.W0,TinySRModel.W1,true);
        conv(a,b,w,h,8,8,TinySRModel.W2,TinySRModel.W3,true);
        conv(b,out,w,h,8,3,TinySRModel.W4,TinySRModel.W5,false);
        for(int i=0;i<N;i++){
            int rr=clamp(Math.round((in[i]+out[i])*255));
            int gg=clamp(Math.round((in[N+i]+out[N+i])*255));
            int bb=clamp(Math.round((in[2*N+i]+out[2*N+i])*255));
            px[i]=0xff000000|(rr<<16)|(gg<<8)|bb;
        }
        Bitmap dst=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); dst.setPixels(px,0,w,0,0,w,h); return dst;
    }
    private static void conv(float[] in,float[] out,int w,int h,int cin,int cout,float[] weights,float[] bias,boolean relu){
        int n=w*h;
        for(int oc=0;oc<cout;oc++) for(int y=0;y<h;y++) for(int x=0;x<w;x++){
            float s=bias[oc];
            for(int ic=0;ic<cin;ic++) for(int ky=-1;ky<=1;ky++) for(int kx=-1;kx<=1;kx++){
                int xx=x+kx, yy=y+ky; if(xx<0||xx>=w||yy<0||yy>=h) continue;
                int wi=(((oc*cin+ic)*3+(ky+1))*3+(kx+1));
                s += in[ic*n+yy*w+xx]*weights[wi];
            }
            if(relu)s=relu(s); out[oc*n+y*w+x]=s;
        }
    }
    private TinySR(){}
}
