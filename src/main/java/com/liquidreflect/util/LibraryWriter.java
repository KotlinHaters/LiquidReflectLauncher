package com.liquidreflect.util;

import com.liquidreflect.MainApp;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LibraryWriter extends ClassWriter {

    public LibraryWriter(int flags) {
        super(flags);
    }

    public static List<String> getSuperClasses(String type) {
        final List<String> list = new ArrayList<>();
        do {
            list.add(type);
            final ClassNode classNode = MainApp.nodeProvider.get(type);
            if (classNode == null) {
                throw new TypeNotPresentException(type, null);
            }
            type = classNode.superName;
        } while (type != null);
        Collections.reverse(list);
        return list;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        final List<String> parentTree1 = getSuperClasses(type1);
        final List<String> parentTree2 = getSuperClasses(type2);
        final int size = Math.min(parentTree1.size(), parentTree2.size());
        int i = 0;
        while (i < size && parentTree1.get(i).equals(parentTree2.get(i))) {
            i++;
        }
        if (i == 0) {
            return "java/lang/Object";
        }
        return parentTree1.get(i - 1);
    }
}
