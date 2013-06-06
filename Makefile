.SUFFIXES: .java .m
.PHONY: default clean translate link

include ../resources/make/common.mk

MAIN_SOURCES = $(shell find $(MAIN_SRC_DIR) -name *.java)
MAIN_TEMP_SOURCES = $(subst $(MAIN_SRC_DIR), $(OPERATION_GEN_DIR), $(MAIN_SOURCES))
MAIN_GEN_SOURCES = $(MAIN_TEMP_SOURCES:.java=.m)
MAIN_TEMP_SOURCES2 = $(subst $(MAIN_SRC_DIR), $(BUILD_DIR)/main, $(MAIN_SOURCES))
MAIN_OBJECTS = $(MAIN_TEMP_SOURCES2:.java=.o)
JAVA_LIB = $(BUILD_DIR)/libOperation.a

TEST_SOURCES = $(shell find $(TEST_SRC_DIR) -name *.java)
TEST_TEMP_SOURCES = $(subst $(TEST_SRC_DIR), $(TEST_GEN_DIR), $(TEST_SOURCES))
TEST_GEN_SOURCES = $(TEST_TEMP_SOURCES:.java=.m)
TEST_TEMP_SOURCES2 = $(subst $(TEST_SRC_DIR), $(BUILD_DIR)/test, $(TEST_SOURCES))
TEST_OBJECTS = $(TEST_TEMP_SOURCES2:.java=.o)
TEST_BINS = $(TEST_TEMP_SOURCES2:.java=)

CLASSPATH = $(M2_REPO)/com/goodow/gwt/gwt-elemental/2.5.1-SNAPSHOT/gwt-elemental-2.5.1-SNAPSHOT.jar
    
default: clean translate test

translate: translate_main translate_test

pre_translate_main: $(OPERATION_GEN_DIR)
	@rm -f $(MAIN_SOURCE_LIST)
	@mkdir -p `dirname $(MAIN_SOURCE_LIST)`
	@touch $(MAIN_SOURCE_LIST)
        
$(OPERATION_GEN_DIR)/%.m $(OPERATION_GEN_DIR)/%.h: $(MAIN_SRC_DIR)/%.java
	@echo $? >> $(MAIN_SOURCE_LIST)

translate_main: pre_translate_main $(MAIN_GEN_SOURCES)
	@if [ `cat $(MAIN_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath $(MAIN_SRC_DIR) -d $(OPERATION_GEN_DIR) \
	    -classpath $(CLASSPATH) \
	    `cat $(MAIN_SOURCE_LIST)` ; \
	fi
	@cd $(OPERATION_GEN_DIR);mkdir -p ../include;tar -c . | tar -x -C ../include --include=*.h

$(BUILD_DIR)/main/%.o: $(OPERATION_GEN_DIR)/%.m $(MAIN_SRC_DIR)/%.java
	@mkdir -p `dirname $@`
	@$(J2OBJCC) -c $< -o $@ -g -I$(OPERATION_GEN_DIR) -I$(ELEMENTAL_GEN_DIR)

$(JAVA_LIB): $(MAIN_OBJECTS)
	libtool -static -o $(JAVA_LIB) $(MAIN_OBJECTS) $(ELEMENTAL_DIR)/target/j2objc/libElemental.a

link: translate $(JAVA_LIB)

pre_translate_test: $(TEST_GEN_DIR)
	@rm -f $(TEST_SOURCE_LIST)
	@mkdir -p `dirname $(TEST_SOURCE_LIST)`
	@touch $(TEST_SOURCE_LIST)

$(TEST_GEN_DIR)/%.m $(TEST_GEN_DIR)/%.h: $(TEST_SRC_DIR)/%.java
	@echo $? >> $(TEST_SOURCE_LIST)

translate_test: pre_translate_test $(TEST_GEN_SOURCES)
	@if [ `cat $(TEST_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath $(MAIN_SRC_DIR):$(TEST_SRC_DIR) -d $(TEST_GEN_DIR) \
	    -classpath $(CLASSPATH):$(JUNIT_JAR) -Werror -use-arc \
	    `cat $(TEST_SOURCE_LIST)` ; \
	fi

$(BUILD_DIR)/test/%.o: $(TEST_GEN_DIR)/%.m $(TEST_SRC_DIR)/%.java
	@mkdir -p `dirname $@`
	@$(J2OBJCC) -c $< -o $@ \
	  -g -I$(OPERATION_GEN_DIR) -I$(ELEMENTAL_GEN_DIR) -I$(TEST_GEN_DIR) \
	  -Wno-objc-redundant-literal-use -Wno-format \
	  -Werror -Wno-parentheses

$(BUILD_DIR)/test/%: $(BUILD_DIR)/test/%.o $(JAVA_LIB)
	@$(J2OBJCC) $< -o $@ \
	  -g -Werror \
	  -ljunit -lOperation -L$(BUILD_DIR)

link_test: link translate_test $(TEST_OBJECTS) $(TEST_BINS)

test: link_test $(TEST_BINS)
	/bin/sh $(ROOT_DIR)/resources/make/runtests.sh $(TEST_BINS)

$(OPERATION_GEN_DIR):
	@mkdir -p $(OPERATION_GEN_DIR)
$(TEST_GEN_DIR):
	@mkdir -p $(TEST_GEN_DIR)
$(BUILD_DIR):
	@mkdir -p $(BUILD_DIR)/main
	@mkdir -p $(BUILD_DIR)/test

clean:
	@rm -rf $(OPERATION_GEN_DIR) $(TEST_GEN_DIR) $(BUILD_DIR)
