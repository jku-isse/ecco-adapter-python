from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters
import sys
import ast
import pickle

# ArtifactTree to AST
def parse(javaNode) -> ast.AST:

    if ep.isType(javaNode):
        bytes = ep.geTypeArtifactBytes(javaNode)
        curr = pickle.loads(bytes)

        fieldNodes = javaNode.getChildren()
        for idx in range(len(fieldNodes)):
            fieldNode = fieldNodes[idx]
            fieldName = ep.getFieldArtifactName(fieldNode)

            if ep.isOrdered(fieldNode):
                fields = []
                # add all fields
                childNodes = fieldNode.getChildren()
                for idx in range(len(childNodes)):
                    childNode = childNodes[idx]
                    parsedChild = parse(childNode)
                    fields.append(parsedChild)
                setattr(curr, fieldName, fields)
    #
    #         # elif ep.isField(fieldNode):
    #         #     print(fieldName)
    #         else: # only 1 child
    #             childNode = fieldNode.getChildren()[0]
    #             parsedChild = parse(childNode)
    #             setattr(curr, fieldName, parsedChild)
    # elif ep.isDump(javaNode):
    #     dump = ep.getDumpArtifactBytes(javaNode)
    #     curr = pickle.loads(dump)
    # elif ep.isString(javaNode):
    #     curr = ep.getStringArtifactString(javaNode)
    # elif ep.isInt(javaNode):
    #     curr = ep.getIntArtifactValue(javaNode)
    # else:
    #     print("not a known artifact")

    return curr


print(f"\nPY: Starting Writer Script for {0}", sys.argv[1])

gateway = JavaGateway()
ep = gateway.entry_point


# parse code from Java Artifact Tree
code = parse(ep.getRoot())
print(ast.dump(code))

# open python file
f = open(sys.argv[1], "w", -1, "UTF-8")
f.write(ast.unparse(code))
f.close()

print("\nPY: Finished Script")
