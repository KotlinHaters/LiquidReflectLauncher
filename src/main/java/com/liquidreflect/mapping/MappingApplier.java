package com.liquidreflect.mapping;

import com.liquidreflect.util.Logger;
import org.objectweb.asm.tree.*;

public class MappingApplier {
    private  final Mapping mapping;
    private final Logger logger = new Logger();

    public MappingApplier(Mapping mapping){
        this.mapping = mapping;
    }

    public void apply(ClassNode classNode){
        if(classNode.name.startsWith("org")) return;
        for(MethodNode methodNode : classNode.methods){
            logger.info("Mapping Method: "+classNode.name + "/" + methodNode.name + " " + methodNode.desc);
            for(AbstractInsnNode insnNode : methodNode.instructions){
                if(insnNode instanceof MethodInsnNode){
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                    methodInsnNode.name = mapping.mapMethodName(methodInsnNode.owner , methodInsnNode.name);
                    }
                if(insnNode instanceof FieldInsnNode){
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
                    fieldInsnNode.name = mapping.mapFieldName(fieldInsnNode.owner ,fieldInsnNode.name);
                }
            }
        }
    }
}
