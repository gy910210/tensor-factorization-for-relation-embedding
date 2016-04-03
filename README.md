Matrix Factorization for Relation Embedding
============================================
## Introduction
Realtion is the key component of knowledge base, for example `isA` relation, `birth_in` relation, `dependency` relation in sentences and so on. This relations can be represented as a triple as `<lEntity, rel, rEntity>`. This proposal gives an innovation idea to generate relation and entity embedding by a matrix factorization approach. 

## Approach
We can represent entities and relations in the latent vector space, that is to say we represent an entity as a `n demension vector` and a relation as `n*n demension matrix`. Given a specific relation `<l, rel, r>`, we can give it a score as `El_T * Rel * Er` (vector * matrix * vector). To make it a classification problem, we can use Sigmoid or Hinge loss function as output.

The training data is the relations of a knowledge base or dependencies of all sentences, represented as a list of `<lEntity, rel, rEntity>` triples. Because there are only positive data, we should produce negtive data. Here, we use random negtive sampling method to produce negtive data when training. Now, we use the simple SGD approach to train the model.

## Evaluation
Our approach uses only the relation data of knowledge bases and do not use any extern resources, so our approach is more flexible and can be used in any knowledge base or anything with relations. 

Our approach will produce the embedding of entities and relations after training, we can use it to predict relations given any two components of the triple `<lEntity, rel, rEntity>`. And the embeddings can also be used in other tasks if needed.
