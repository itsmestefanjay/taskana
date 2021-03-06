package pro.taskana.classification.rest;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pro.taskana.classification.api.ClassificationQuery;
import pro.taskana.classification.api.ClassificationService;
import pro.taskana.classification.api.exceptions.ClassificationAlreadyExistException;
import pro.taskana.classification.api.exceptions.ClassificationInUseException;
import pro.taskana.classification.api.exceptions.ClassificationNotFoundException;
import pro.taskana.classification.api.models.Classification;
import pro.taskana.classification.api.models.ClassificationSummary;
import pro.taskana.classification.rest.assembler.ClassificationRepresentationModelAssembler;
import pro.taskana.classification.rest.assembler.ClassificationSummaryRepresentationModelAssembler;
import pro.taskana.classification.rest.models.ClassificationRepresentationModel;
import pro.taskana.classification.rest.models.ClassificationSummaryRepresentationModel;
import pro.taskana.common.api.BaseQuery.SortDirection;
import pro.taskana.common.api.exceptions.ConcurrencyException;
import pro.taskana.common.api.exceptions.DomainNotFoundException;
import pro.taskana.common.api.exceptions.InvalidArgumentException;
import pro.taskana.common.api.exceptions.NotAuthorizedException;
import pro.taskana.common.rest.AbstractPagingController;
import pro.taskana.common.rest.Mapping;
import pro.taskana.common.rest.models.TaskanaPagedModel;

/** Controller for all {@link Classification} related endpoints. */
@RestController
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class ClassificationController extends AbstractPagingController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationController.class);

  private static final String LIKE = "%";

  private static final String NAME = "name";

  private static final String NAME_LIKE = "name-like";

  private static final String KEY = "key";

  private static final String DOMAIN = "domain";

  private static final String CATEGORY = "category";

  private static final String TYPE = "type";

  private static final String CUSTOM_1_LIKE = "custom-1-like";

  private static final String CUSTOM_2_LIKE = "custom-2-like";

  private static final String CUSTOM_3_LIKE = "custom-3-like";

  private static final String CUSTOM_4_LIKE = "custom-4-like";

  private static final String CUSTOM_5_LIKE = "custom-5-like";

  private static final String CUSTOM_6_LIKE = "custom-6-like";

  private static final String CUSTOM_7_LIKE = "custom-7-like";

  private static final String CUSTOM_8_LIKE = "custom-8-like";

  private static final String SORT_BY = "sort-by";

  private static final String SORT_DIRECTION = "order";

  private final ClassificationService classificationService;

  private final ClassificationRepresentationModelAssembler
      classificationRepresentationModelAssembler;

  private final ClassificationSummaryRepresentationModelAssembler
      classificationSummaryRepresentationModelAssembler;

  ClassificationController(
      ClassificationService classificationService,
      ClassificationRepresentationModelAssembler classificationRepresentationModelAssembler,
      ClassificationSummaryRepresentationModelAssembler
          classificationSummaryRepresentationModelAssembler) {
    this.classificationService = classificationService;
    this.classificationRepresentationModelAssembler = classificationRepresentationModelAssembler;
    this.classificationSummaryRepresentationModelAssembler =
        classificationSummaryRepresentationModelAssembler;
  }

  @GetMapping(path = Mapping.URL_CLASSIFICATIONS)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<TaskanaPagedModel<ClassificationSummaryRepresentationModel>>
      getClassifications(
      @RequestParam MultiValueMap<String, String> params)
      throws InvalidArgumentException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Entry to getClassifications(params= {})", params);
    }

    ClassificationQuery query = classificationService.createClassificationQuery();
    query = applySortingParams(query, params);
    applyFilterParams(query, params);

    PageMetadata pageMetadata = getPageMetadata(params, query);
    List<ClassificationSummary> classificationSummaries = getQueryList(query, pageMetadata);

    ResponseEntity<TaskanaPagedModel<ClassificationSummaryRepresentationModel>> response =
        ResponseEntity.ok(
            classificationSummaryRepresentationModelAssembler.toPageModel(
                classificationSummaries, pageMetadata));
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Exit from getClassifications(), returning {}", response);
    }

    return response;
  }

  @GetMapping(path = Mapping.URL_CLASSIFICATIONS_ID, produces = MediaTypes.HAL_JSON_VALUE)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> getClassification(
      @PathVariable String classificationId) throws ClassificationNotFoundException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Entry to getClassification(classificationId= {})", classificationId);
    }

    Classification classification = classificationService.getClassification(classificationId);
    ResponseEntity<ClassificationRepresentationModel> response =
        ResponseEntity.ok(classificationRepresentationModelAssembler.toModel(classification));
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Exit from getClassification(), returning {}", response);
    }

    return response;
  }

  @PostMapping(path = Mapping.URL_CLASSIFICATIONS)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> createClassification(
      @RequestBody ClassificationRepresentationModel resource)
      throws NotAuthorizedException, ClassificationAlreadyExistException, DomainNotFoundException,
          InvalidArgumentException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Entry to createClassification(resource= {})", resource);
    }
    Classification classification = classificationRepresentationModelAssembler
                                        .toEntityModel(resource);
    classification = classificationService.createClassification(classification);

    ResponseEntity<ClassificationRepresentationModel> response =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(classificationRepresentationModelAssembler.toModel(classification));
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Exit from createClassification(), returning {}", response);
    }

    return response;
  }

  @PutMapping(path = Mapping.URL_CLASSIFICATIONS_ID)
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> updateClassification(
      @PathVariable(value = "classificationId") String classificationId,
      @RequestBody ClassificationRepresentationModel resource)
      throws NotAuthorizedException, ClassificationNotFoundException, ConcurrencyException,
          InvalidArgumentException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Entry to updateClassification(classificationId= {}, resource= {})",
          classificationId,
          resource);
    }

    ResponseEntity<ClassificationRepresentationModel> result;
    if (classificationId.equals(resource.getClassificationId())) {
      Classification classification = classificationRepresentationModelAssembler
                                          .toEntityModel(resource);
      classification = classificationService.updateClassification(classification);
      result =
          ResponseEntity.ok(classificationRepresentationModelAssembler.toModel(classification));
    } else {
      throw new InvalidArgumentException(
          "ClassificationId ('"
              + classificationId
              + "') of the URI is not identical with the classificationId ('"
              + resource.getClassificationId()
              + "') of the object in the payload.");
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Exit from updateClassification(), returning {}", result);
    }

    return result;
  }

  @DeleteMapping(path = Mapping.URL_CLASSIFICATIONS_ID)
  @Transactional(readOnly = true, rollbackFor = Exception.class)
  public ResponseEntity<ClassificationRepresentationModel> deleteClassification(
      @PathVariable String classificationId)
      throws ClassificationNotFoundException, ClassificationInUseException, NotAuthorizedException {
    LOGGER.debug("Entry to deleteClassification(classificationId= {})", classificationId);
    classificationService.deleteClassification(classificationId);
    ResponseEntity<ClassificationRepresentationModel> response = ResponseEntity.noContent().build();
    LOGGER.debug("Exit from deleteClassification(), returning {}", response);
    return response;
  }

  private ClassificationQuery applySortingParams(
      ClassificationQuery query, MultiValueMap<String, String> params)
      throws InvalidArgumentException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Entry to applySortingParams(query= {}, params= {})", query, params);
    }

    // sorting
    String sortBy = params.getFirst(SORT_BY);
    if (sortBy != null) {
      SortDirection sortDirection;
      if (params.getFirst(SORT_DIRECTION) != null
              && "desc".equals(params.getFirst(SORT_DIRECTION))) {
        sortDirection = SortDirection.DESCENDING;
      } else {
        sortDirection = SortDirection.ASCENDING;
      }
      switch (sortBy) {
        case (CATEGORY):
          query = query.orderByCategory(sortDirection);
          break;
        case (DOMAIN):
          query = query.orderByDomain(sortDirection);
          break;
        case (KEY):
          query = query.orderByKey(sortDirection);
          break;
        case (NAME):
          query = query.orderByName(sortDirection);
          break;
        default:
          throw new InvalidArgumentException("Unknown order '" + sortBy + "'");
      }
    }
    params.remove(SORT_BY);
    params.remove(SORT_DIRECTION);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Exit from applySortingParams(), returning {}", query);
    }

    return query;
  }

  private void applyFilterParams(
      ClassificationQuery query, MultiValueMap<String, String> params)
      throws InvalidArgumentException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Entry to applyFilterParams(query= {}, params= {})", query, params);
    }

    if (params.containsKey(NAME)) {
      String[] names = extractCommaSeparatedFields(params.get(NAME));
      query.nameIn(names);
      params.remove(NAME);
    }
    if (params.containsKey(NAME_LIKE)) {
      query.nameLike(LIKE + params.get(NAME_LIKE).get(0) + LIKE);
      params.remove(NAME_LIKE);
    }
    if (params.containsKey(KEY)) {
      String[] names = extractCommaSeparatedFields(params.get(KEY));
      query.keyIn(names);
      params.remove(KEY);
    }
    if (params.containsKey(CATEGORY)) {
      String[] names = extractCommaSeparatedFields(params.get(CATEGORY));
      query.categoryIn(names);
      params.remove(CATEGORY);
    }
    if (params.containsKey(DOMAIN)) {
      String[] names = extractCommaSeparatedFields(params.get(DOMAIN));
      query.domainIn(names);
      params.remove(DOMAIN);
    }
    if (params.containsKey(TYPE)) {
      String[] names = extractCommaSeparatedFields(params.get(TYPE));
      query.typeIn(names);
      params.remove(TYPE);
    }
    if (params.containsKey(CUSTOM_1_LIKE)) {
      query.customAttributeLike("1", LIKE + params.get(CUSTOM_1_LIKE).get(0) + LIKE);
      params.remove(CUSTOM_1_LIKE);
    }
    if (params.containsKey(CUSTOM_2_LIKE)) {
      query.customAttributeLike("2", LIKE + params.get(CUSTOM_2_LIKE).get(0) + LIKE);
      params.remove(CUSTOM_2_LIKE);
    }
    if (params.containsKey(CUSTOM_3_LIKE)) {
      query.customAttributeLike("3", LIKE + params.get(CUSTOM_3_LIKE).get(0) + LIKE);
      params.remove(CUSTOM_3_LIKE);
    }
    if (params.containsKey(CUSTOM_4_LIKE)) {
      query.customAttributeLike("4", LIKE + params.get(CUSTOM_4_LIKE).get(0) + LIKE);
      params.remove(CUSTOM_4_LIKE);
    }
    if (params.containsKey(CUSTOM_5_LIKE)) {
      query.customAttributeLike("5", LIKE + params.get(CUSTOM_5_LIKE).get(0) + LIKE);
      params.remove(CUSTOM_5_LIKE);
    }
    if (params.containsKey(CUSTOM_6_LIKE)) {
      query.customAttributeLike("6", LIKE + params.get(CUSTOM_6_LIKE).get(0) + LIKE);
      params.remove(CUSTOM_6_LIKE);
    }
    if (params.containsKey(CUSTOM_7_LIKE)) {
      query.customAttributeLike("7", LIKE + params.get(CUSTOM_7_LIKE).get(0) + LIKE);
      params.remove(CUSTOM_7_LIKE);
    }
    if (params.containsKey(CUSTOM_8_LIKE)) {
      query.customAttributeLike("8", LIKE + params.get(CUSTOM_8_LIKE).get(0) + LIKE);
      params.remove(CUSTOM_8_LIKE);
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Exit from applyFilterParams(), returning {}", query);
    }

  }
}
