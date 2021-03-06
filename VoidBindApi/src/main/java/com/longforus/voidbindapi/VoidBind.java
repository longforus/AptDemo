package com.longforus.voidbindapi;

public class VoidBind {
    public static void bind(Object activity) {
        try {
            //反射对应的类,生成实例调用
            Class clazz = Class.forName(activity.getClass().getCanonicalName() + "$VoidBind");
            IBind instance = (IBind)clazz.newInstance();
            //调用接口方法,为bind的view赋值
            instance.bind(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
