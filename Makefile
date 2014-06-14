.SUFFIXES: .java .m
.PHONY: default clean translate link

include ../resources/make/common.mk
# J2OBJC_DIST = ../realtime-store/Project/Pods/J2ObjC/dist

OPERATION_GEN_DIR = ../realtime-store/GDStore/Classes/generated
MAIN_SOURCES = $(subst $(MAIN_SRC_DIR)/,,$(shell find $(MAIN_SRC_DIR) -name *.java))
MAIN_GEN_SOURCES = $(MAIN_SOURCES:%.java=$(OPERATION_GEN_DIR)/%.m)
MAIN_OBJECTS = $(MAIN_SOURCES:%.java=$(BUILD_DIR)/main/%.o)
SUPPORT_LIB = $(BUILD_DIR)/libOperation.a

TEST_SOURCES = $(subst $(TEST_SRC_DIR)/,,$(shell find $(TEST_SRC_DIR) -name *.java))
TEST_GEN_SOURCES = $(TEST_SOURCES:%.java=$(TEST_GEN_DIR)/%.m)
TEST_OBJECTS = $(TEST_SOURCES:%.java=$(BUILD_DIR)/test/%.o)

TEMP_PATH = $(M2_REPO)/com/goodow/realtime/realtime-json/0.5.5-SNAPSHOT/realtime-json-0.5.5-SNAPSHOT.jar
CLASSPATH = $(shell echo $(TEMP_PATH) | sed 's/ //g')

default: clean translate

translate: translate_main translate_test

pre_translate_main: $(BUILD_DIR) $(OPERATION_GEN_DIR)
	@rm -f $(MAIN_SOURCE_LIST)
	@touch $(MAIN_SOURCE_LIST)
        
$(OPERATION_GEN_DIR)/%.m $(OPERATION_GEN_DIR)/%.h: $(MAIN_SRC_DIR)/%.java
	@echo $? >> $(MAIN_SOURCE_LIST)

translate_main: pre_translate_main $(MAIN_GEN_SOURCES)
	@if [ `cat $(MAIN_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath $(MAIN_SRC_DIR) -d $(OPERATION_GEN_DIR) \
	    -classpath $(CLASSPATH) \
	    `cat $(MAIN_SOURCE_LIST)` ; \
	fi

$(BUILD_DIR)/main/%.o: $(OPERATION_GEN_DIR)/%.m $(MAIN_SRC_DIR)/%.java
	@mkdir -p `dirname $@`
	@$(J2OBJCC) -c $< -o $@ -g -I$(OPERATION_GEN_DIR) -I$(ELEMENTAL_GEN_DIR)

$(SUPPORT_LIB): $(MAIN_OBJECTS)
	libtool -static -o $(SUPPORT_LIB) $(MAIN_OBJECTS) $(ELEMENTAL_DIR)/target/j2objc/libElemental.a

link: translate $(SUPPORT_LIB)

pre_translate_test: $(BUILD_DIR) $(TEST_GEN_DIR)
	@rm -f $(TEST_SOURCE_LIST)
	@touch $(TEST_SOURCE_LIST)

$(TEST_GEN_DIR)/%.m $(TEST_GEN_DIR)/%.h: $(TEST_SRC_DIR)/%.java
	@echo $? >> $(TEST_SOURCE_LIST)

translate_test: pre_translate_test $(TEST_GEN_SOURCES)
	@if [ `cat $(TEST_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath $(MAIN_SRC_DIR):$(TEST_SRC_DIR) -d $(TEST_GEN_DIR) \
	    -classpath $(CLASSPATH):$(JUNIT_JAR) -Werror \
	    `cat $(TEST_SOURCE_LIST)` ; \
	fi

$(BUILD_DIR)/test/%.o: $(TEST_GEN_DIR)/%.m $(TEST_SRC_DIR)/%.java
	@mkdir -p `dirname $@`
	@$(J2OBJCC) -c $< -o $@ \
	  -g -I$(OPERATION_GEN_DIR) -I$(ELEMENTAL_GEN_DIR) -I$(TEST_GEN_DIR) \
	  -Wno-objc-redundant-literal-use -Wno-format \
	  -Werror -Wno-parentheses

$(TEST_BIN): $(TEST_OBJECTS) $(SUPPORT_LIB)
	@$(J2OBJCC) -o $@ $(TEST_OBJECTS) \
	  -g -Werror \
	  -ljunit -lOperation -L$(BUILD_DIR)

link_test: link translate_test $(TEST_OBJECTS) $(TEST_BIN)

test: link_test $(TEST_BIN)
	/bin/sh $(ROOT_DIR)/resources/make/runtests.sh $(TEST_BIN) $(subst /,.,$(TEST_SOURCES:%.java=%))

$(OPERATION_GEN_DIR):
	@mkdir -p $(OPERATION_GEN_DIR)
$(TEST_GEN_DIR):
	@mkdir -p $(TEST_GEN_DIR)
$(BUILD_DIR):
	@mkdir -p $(BUILD_DIR)/main
	@mkdir -p $(BUILD_DIR)/test

clean:
	@rm -rf $(OPERATION_GEN_DIR)/com/goodow/realtime/operation/ $(TEST_GEN_DIR) $(BUILD_DIR)
