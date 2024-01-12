package com.liquidreflect.mapping;

import org.objectweb.asm.tree.ClassNode;

public interface NodeProvider {
    ClassNode get(String name);
}
