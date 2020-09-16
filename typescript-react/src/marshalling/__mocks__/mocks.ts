import { Marshaller, Serialized } from '../Marshaller';

/*
* Fake "DSL generated" code for:
*
* ```dsl
* value Pet {
*   String name;
* }
* ```
*/
export class Pet {
  static serialize = (it: Pet | undefined, isRequired: boolean, path?: string): Serialized<Pet> | undefined => {
    if (Pet.required.length === 0 && it == null && isRequired) {
      return {} as any; // Hack since we can't infer whether required is empty externally
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    const pathForChildren = path == null ? '' : `${path}.`;

    return {
      name: Pet.serializeName(it!.name, `${pathForChildren}name`),
    };
  }

  static deserialize = (it: Serialized<Pet> | undefined, isRequired: boolean, path?: string): Serialized<Pet> | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    const pathForChildren = path == null ? '' : `${path}.`;

    return {
      name: Pet.deserializeName(it!.name, `${pathForChildren}name`),
    };
  }

  private static required = ['name'];
  private static serializeName = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'String', true, path);
  private static deserializeName = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'String', true, path);

  public name: string | undefined;
}

/*
* Fake "DSL generated" code for:
*
* ```dsl
* value Person {
*   String name;
*   Int?   age;
*   List<Int> tagIDs;
*   Set<Pet>? pets;
* }
* ```
*/
export class Person {
  static serialize = (it: Person | undefined, isRequired: boolean, path?: string): Serialized<Person> | undefined => {
    if (Person.required.length === 0 && it == null && isRequired) {
      return {} as any; // Hack since we can't infer whether required is empty externally
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    const pathForChildren = path == null ? '' : `${path}.`;

    return {
      age: Person.serializeAge(it!.age, `${pathForChildren}age`),
      name: Person.serializeName(it!.name, `${pathForChildren}name`),
      pets: Person.serializePets(it!.pets, `${pathForChildren}pets`)! as any,
      tagIDs: Person.serializeTagIDs(it!.tagIDs, `${pathForChildren}tagIDs`)!,
    };
  }

  static deserialize = (it: Serialized<Person> | undefined, isRequired: boolean, path?: string): Serialized<Person> | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    const pathForChildren = path == null ? '' : `${path}.`;

    return {
      age: Person.deserializeAge(it!.age, `${pathForChildren}age`),
      name: Person.deserializeName(it!.name, `${pathForChildren}name`),
      pets: Person.deserializePets(it!.pets, `${pathForChildren}pets`) as any,
      tagIDs: Person.deserializeTagIDs(it!.tagIDs, `${pathForChildren}tagIDs`)!,
    };
  }

  private static required = ['name', 'tagIDs'];
  private static serializeAge = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'Int', false, path);
  private static deserializeAge = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Int', false, path);
  private static serializeName = (it: any, path: string) =>  Marshaller.getInstance().serialize(it, 'String', true, path);
  private static deserializeName = (it: any, path: string) =>  Marshaller.getInstance().deserialize(it, 'String', true, path);
  private static serializePets = (it: any, path: string) => Marshaller.Util.mapSeq(
    Marshaller.getInstance().serialize(it, 'Set', false, path),
    Person.serializePetsMember,
    path,
  )
  private static serializePetsMember =
    (it: any, path: string) => Marshaller.getInstance().serializeWith(it, 'Pet', (it: Pet) => Pet.serialize(it, true, path), true, path)
  private static deserializePets = (it: any, path: string) => Marshaller.getInstance().deserialize(
    Marshaller.Util.mapSeq(
      it,
      Person.deserializePetsMember,
      path,
    ),
    'Set',
    false,
    path,
  )
  private static deserializePetsMember =
    (it: any, path: string) => Marshaller.getInstance().deserializeWith(it, 'Pet', (it: Pet) => Pet.deserialize(it, true, path), true, path)
  private static serializeTagIDs = (it: any, path: string) => Marshaller.Util.mapSeq(
    Marshaller.getInstance().serialize(it, 'List', true, path),
    Person.serializeTagIDsMember,
    path,
  )
  private static serializeTagIDsMember =
    (it: any, path: string) => Marshaller.getInstance().serialize(it, 'Int', true, path)
  private static deserializeTagIDs = (it: any, path: string) => Marshaller.getInstance().deserialize(
    Marshaller.Util.mapSeq(
      it,
      Person.deserializeTagIDsMember,
      path,
    ),
    'List',
    true,
    path,
  )
  private static deserializeTagIDsMember =
    (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Int', true, path)

  public name?: string;
  public age?: Int;
  public tagIDs?: Int[];
  public pets?: Set<Pet>;
}

/*
* Fake "DSL generated" code for:
*
* ```dsl
* value OptionalFields {
*   String? name;
*   Long? locationID;
* }
* ```
*/
export class OptionalFields {
  static serialize = (it: OptionalFields | undefined, isRequired: boolean, path?: string): Serialized<OptionalFields> | undefined => {
    if (OptionalFields.required.length === 0 && it == null && isRequired) {
      return {} as any; // Hack since we can't infer whether required is empty externally
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    const pathForChildren = path == null ? '' : `${path}.`;

    return {
      locationID: OptionalFields.serializeLocationID(it!.locationID, `${pathForChildren}locationID`),
      name: OptionalFields.serializeName(it!.name, `${pathForChildren}name`),
    };
  }

  static deserialize = (it: Serialized<OptionalFields> | undefined, isRequired: boolean, path?: string): Serialized<OptionalFields> | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    const pathForChildren = path == null ? '' : `${path}.`;

    return {
      locationID: OptionalFields.deserializeLocationID(it!.locationID, `${pathForChildren}locationID`),
      name: OptionalFields.deserializeName(it!.name, `${pathForChildren}name`),
    };
  }

  private static required = [];
  private static serializeName = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'String', false, path);
  private static deserializeName = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'String', false, path);
  private static serializeLocationID = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'Long', false, path);
  private static deserializeLocationID = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Long', false, path);

  public name?: string;
  public locationID?: Long;
}
