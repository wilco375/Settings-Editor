package com.wilco375.settingseditor.object.serializable;


import android.annotation.TargetApi;
import android.os.UserHandle;

import java.lang.reflect.Constructor;

@TargetApi(17)
public class SerializableUserHandle {
    public int user;

    public SerializableUserHandle(UserHandle handle) {
        try {
            this.user = (int) handle.getClass().getMethod("getIdentifier").invoke(handle);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public SerializableUserHandle(int user) {
        this.user = user;
    }

    public UserHandle toUserHandle() {
        try {
            Class<UserHandle> userHandle = UserHandle.class;
            Constructor constructor = userHandle.getConstructor(int.class);
            return (UserHandle) constructor.newInstance((int) user);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
