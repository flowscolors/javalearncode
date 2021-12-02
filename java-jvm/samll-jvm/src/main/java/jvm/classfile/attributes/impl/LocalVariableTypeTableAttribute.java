package jvm.classfile.attributes.impl;

import jvm.classfile.attributes.AttributeInfo;
import jvm.classfile.ClassReader;
import jvm.classfile.attributes.AttributeInfo;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/26
 */
public class LocalVariableTypeTableAttribute implements AttributeInfo {

    private LocalVariableTypeTableEntry[] localVariableTypeTable;

    @Override
    public void readInfo(ClassReader reader) {
        int localVariableTypeTableLength = reader.readUnit16();
        this.localVariableTypeTable = new LocalVariableTypeTableEntry[localVariableTypeTableLength];
        for (int i = 0; i < localVariableTypeTableLength; i++) {
            this.localVariableTypeTable[i] = new LocalVariableTypeTableEntry(reader.readUnit16(), reader.readUnit16(), reader.readUnit16(), reader.readUnit16(), reader.readUnit16());
        }
    }

    static class LocalVariableTypeTableEntry {
        private int startPC;
        private int length;
        private int nameIndex;
        private int signatureIndex;
        private int index;

        public LocalVariableTypeTableEntry(int startPC, int length, int nameIndex, int signatureIndex, int index) {
            this.startPC = startPC;
            this.length = length;
            this.nameIndex = nameIndex;
            this.signatureIndex = signatureIndex;
            this.index = index;
        }
    }

}
