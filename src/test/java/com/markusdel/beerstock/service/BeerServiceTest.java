package com.markusdel.beerstock.service;

import com.markusdel.beerstock.builder.BeerDTPBuilder;
import com.markusdel.beerstock.dto.BeerDTO;
import com.markusdel.beerstock.entity.Beer;
import com.markusdel.beerstock.exception.BeerAlreadyRegisteredException;
import com.markusdel.beerstock.exception.BeerNotFoundException;
import com.markusdel.beerstock.exception.BeerStockExceededException;
import com.markusdel.beerstock.mapper.BeerMapper;
import com.markusdel.beerstock.repository.BeerRepository;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
    void whenBeerInformedThenItShuldBeCreated() throws BeerAlreadyRegisteredException {
        // given
        BeerDTO expectedBeerDTO = BeerDTPBuilder.builder().build().toBeerDTO();
        Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        //then
        BeerDTO createdBeerDTO = beerService.createdBeer(expectedBeerDTO);

        assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
        assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
        assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));
    }

    @Test
    void whenAlreadyRegisteredBeerInformedThenExceptionShouldBeThrown() throws BeerAlreadyRegisteredException {
        // given
        BeerDTO expectedBeerDTO = BeerDTPBuilder.builder().build().toBeerDTO();
        Beer duplicateBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicateBeer));

        // then
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createdBeer(expectedBeerDTO));
    }

    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTPBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        // when
        when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));

        // then
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());

        assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() throws BeerNotFoundException {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTPBuilder.builder().build().toBeerDTO();

        // when
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());

        // then
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
    }

    @Test
    void whenListBeerIsCalledThenReturnAListOfBeers() {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTPBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        // when
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));

        // then
        List<BeerDTO> foundBListBeerDTO = beerService.listAll();

        assertThat(foundBListBeerDTO, is(not(empty())));
        assertThat(foundBListBeerDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
        // when
        when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

        // then
        List<BeerDTO> foundBListBeerDTO = beerService.listAll();

        assertThat(foundBListBeerDTO, is(empty()));
    }

    @Test
    void whenExclusionIsCalledWithValidIdThenBeerShouldBeDeleted() throws BeerNotFoundException {
        // given
        BeerDTO expectedDeleteBeerDTO = BeerDTPBuilder.builder().build().toBeerDTO();
        Beer expectedDeleteBeer = beerMapper.toModel(expectedDeleteBeerDTO);

        // when
        when(beerRepository.findById(expectedDeleteBeerDTO.getId())).thenReturn(Optional.of(expectedDeleteBeer));
        doNothing().when(beerRepository).deleteById(expectedDeleteBeerDTO.getId());

        // then
        beerService.deleteById(expectedDeleteBeerDTO.getId());

        verify(beerRepository, times(1)).findById(expectedDeleteBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedDeleteBeerDTO.getId());
    }

    @Test
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        // given
        BeerDTO expectedBeerDTO = BeerDTPBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        // then
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));

    }

    @Test
    void whenIncrementIsGreatherThanMaxThenThrowException() throws BeerNotFoundException, BeerStockExceededException {
        // given
        BeerDTO expectedBeerDTO = BeerDTPBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 80;

        // then
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() throws BeerNotFoundException, BeerStockExceededException {
        // given
        BeerDTO expectedBeerDTO = BeerDTPBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 45;

        // then
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementItsCalledWithInvalidIdThenThrowException() {
        int quantityToIncrement = 10;

        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }

}
